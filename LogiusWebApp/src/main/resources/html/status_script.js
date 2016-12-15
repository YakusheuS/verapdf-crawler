var URL = "../crawl-job/";

function checkStatus() {
    var jobId = location.search.split("id=")[1];
    $.ajax({url: URL + jobId,
            type:"GET",
            async:false,
	    success: function(result){
            status = result.status;
            number = result.numberOfCrawledUrls;
            crawlUrl = result.url;
            $("#status").text("Job is " + status);
            $("#crawl_url").text("Crawling url " + crawlUrl);
            if(undefined != number)
                $("#number_of_crawled_urls").text(number + " urls crawled.");
            if(status.substring(0, 8) == "Finished") {
                $("#report_link").text(result.reportUrl);
                $("#report_link").html("<a href=\"" + result.reportUrl + "\">Crawl log</a>");
            }
            else {
                $("#report_link").text("");
                setTimeout(checkStatus, 1000);
            }
	    }
	});
}
window.onload = checkStatus;
//$(document).ready(checkStatus());