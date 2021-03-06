package org.verapdf.crawler.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.verapdf.crawler.domain.validation.ValidationError;
import org.verapdf.crawler.domain.validation.VeraPDFValidationResult;
import org.verapdf.crawler.repository.document.ValidatedPDFDao;

import java.io.IOException;
import java.util.Map;

public class VerapdfServiceValidator implements PDFValidator {

    private final static int MAX_VALIDATION_TIMEOUT_IN_MINUTES = 5;
    private final static int MAX_VALIDATION_RETRIES = 2;

    private final String verapdfUrl;
    private final HttpClient httpClient;
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");

    VerapdfServiceValidator(String verapdfUrl) {
        this.verapdfUrl = verapdfUrl;
        httpClient = HttpClientBuilder.create().build();
    }

    @Override
    public boolean validateAndWirteResult(String localFilename, String fileUrl, ValidatedPDFDao validatedPDFDao) throws Exception {
        VeraPDFValidationResult result;
        try {
            result = validate(localFilename, validatedPDFDao);
            while (result == null) {
                logger.info("Could not reach validation service, retry in one minute");
                Thread.sleep(60 * 1000);
                result = validate(localFilename, validatedPDFDao);
            }
        }
        catch (Exception e) {
            logger.error("Error in validation service",e);
            validatedPDFDao.addProcessingError(e.getMessage(), fileUrl);
            return false;
        }
        for(ValidationError error: result.getValidationErrors()) {
            validatedPDFDao.addErrorToDocument(error, fileUrl);
        }
        for(Map.Entry<String, String> property: result.getProperties().entrySet()) {
            validatedPDFDao.insertPropertyForDocument(property.getKey(), property.getValue(), fileUrl);
        }
        validatedPDFDao.addProcessingError(result.getProcessingError(), fileUrl);
        return result.isValid();
    }

    private VeraPDFValidationResult validate(String filename, ValidatedPDFDao validatedPDFDao) throws Exception {
        logger.info("Sending file " + filename + " to validator");
        try {
            sendValidationSettings(validatedPDFDao);
            sendValidationRequest(filename);

            int validationRetries = 0;
            for (int i = 0; i < MAX_VALIDATION_TIMEOUT_IN_MINUTES * 6; i++) {
                int responseCode = getValidationStatus();
                logger.info("Response code is " + responseCode);
                if (responseCode == HttpStatus.SC_OK) { // Vaidation is finished
                    logger.info("Validation is finished");
                    return getValidationResult();
                }
                // Validation is in process
                if (responseCode == HttpStatus.SC_PROCESSING) {
                    logger.info("Validation is in progress");
                    Thread.sleep(10 * 1000);
                    continue;
                }
                // Something went wrong and validation was not finished
                if (responseCode == HttpStatus.SC_CONTINUE) {
                    logger.info("Something went wrong and validation was not finished");
                    validationRetries++;
                    if (validationRetries == MAX_VALIDATION_RETRIES) {
                        throw new Exception("Failed to process document " + filename);
                    }
                    sendValidationSettings(validatedPDFDao);
                    sendValidationRequest(filename);
                    // Reset timeout cycle
                    i = 0;
                } else { // Got unexpected response code
                    throw new Exception("Invalid response code from validation service, code was" + responseCode);
                }
            }
            throw new Exception("Document " + filename + " was not validated in time (" + MAX_VALIDATION_TIMEOUT_IN_MINUTES + " minutes)");
        }
        catch (IOException e) {
            logger.error("Error in validation service", e);
            return null;
        }
    }

    private void sendValidationSettings(ValidatedPDFDao validatedPDFDao) throws IOException {
        HttpPost propertiesPost = new HttpPost(verapdfUrl + "/properties");
        propertiesPost.setHeader("Content-Type", "application/json");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> properties = validatedPDFDao.getPdfPropertiesWithXpath();
        propertiesPost.setEntity(new StringEntity(mapper.writeValueAsString(properties)));
        httpClient.execute(propertiesPost);
        propertiesPost.releaseConnection();
        logger.info("Validation settings have been sent");
    }

    private void sendValidationRequest(String filename) throws IOException {
        HttpPost post = new HttpPost(verapdfUrl);
        post.setEntity(new StringEntity(filename));
        httpClient.execute(post);
        post.releaseConnection();
        logger.info("Validation request have been sent");
    }

    private int getValidationStatus() throws IOException {
        HttpGet get = new HttpGet(verapdfUrl);
        int result = httpClient.execute(get).getStatusLine().getStatusCode();
        get.releaseConnection();
        return result;
    }

    private VeraPDFValidationResult getValidationResult() throws IOException {
        HttpGet get = new HttpGet(verapdfUrl);
        VeraPDFValidationResult result = new ObjectMapper().readValue(httpClient.execute(new HttpGet(verapdfUrl)).getEntity().getContent(), VeraPDFValidationResult.class);
        get.releaseConnection();
        return result;
    }
}