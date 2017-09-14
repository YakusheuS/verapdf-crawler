package org.verapdf.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.verapdf.crawler.configurations.MySqlConfiguration;
import org.verapdf.crawler.configurations.EmailServerConfiguration;
import org.verapdf.crawler.core.heritrix.HeritrixClient;
import org.verapdf.crawler.core.heritrix.HeritrixReporter;
import org.verapdf.crawler.db.document.InsertDocumentDao;
import org.verapdf.crawler.db.document.ReportDocumentDao;
import org.verapdf.crawler.db.document.ValidatedPDFDao;
import org.verapdf.crawler.db.jobs.CrawlRequestDao;
import org.verapdf.crawler.db.jobs.CrawlJobDao;
import org.verapdf.crawler.core.validation.ValidationService;
import org.verapdf.crawler.core.validation.VeraPDFValidator;
import org.verapdf.crawler.resources.*;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourceManager {
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final Logger logger = LoggerFactory.getLogger("CustomLogger");

    private final List<Object> resources = new ArrayList<>();

    public ResourceManager(HeritrixClient client, LogiusConfiguration config) {
        // Initializing all DAO objects
        DataSource dataSource = createMySqlDataSource(config.getMySqlConfiguration());
        CrawlJobDao crawlJobDao = new CrawlJobDao(dataSource);
        CrawlRequestDao crawlRequestDao = new CrawlRequestDao(dataSource);
        ReportDocumentDao reportDocumentDao = new ReportDocumentDao(dataSource);
        ValidatedPDFDao validatedPDFDao = new ValidatedPDFDao(dataSource);
        InsertDocumentDao insertDocumentDao = new InsertDocumentDao(dataSource);

        // Initializing validators and reporters
        HeritrixReporter reporter = new HeritrixReporter(client, reportDocumentDao, crawlJobDao);
        VeraPDFValidator veraPDFValidator = new VeraPDFValidator(config.getVeraPDFServiceConfiguration(), insertDocumentDao, validatedPDFDao, crawlJobDao);
        ValidationService validationService = new ValidationService(dataSource, veraPDFValidator);

        // Initializing resources
        resources.add(new CrawlJobReportResource(crawlJobDao, reporter, validatedPDFDao));
        resources.add(new CrawlJobResource(crawlJobDao, client, crawlRequestDao, reporter, config.getEmailServerConfiguration()));
        resources.add(new CrawlRequestResource(client, crawlRequestDao, crawlJobDao));
        resources.add(new DocumentPropertyResource(reportDocumentDao));
        resources.add(new HeritrixDataResource(validationService, crawlJobDao, dataSource));
        resources.add(new VeraPDFServiceResource(validatedPDFDao));

        // Launching validation
        validationService.start();
        logger.info("Validation service started.");
    }

    public List<Object> getResources() {
        return Collections.unmodifiableList(this.resources);
    }

    private DataSource createMySqlDataSource(MySqlConfiguration credentials) {
        DataSource dataSource = new DriverManagerDataSource();
        ((DriverManagerDataSource)dataSource).setDriverClassName(JDBC_DRIVER);
        ((DriverManagerDataSource)dataSource).setUrl(credentials.getConnectionString());
        ((DriverManagerDataSource)dataSource).setUsername(credentials.getUser());
        ((DriverManagerDataSource)dataSource).setPassword(credentials.getPassword());
        return dataSource;
    }
}
