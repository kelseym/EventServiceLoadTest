import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder

@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.2')



def BASE_URL = 'http://xnat-31.xnat.org/xapi/events/'
def USER = 'admin'
def PASSWORD = 'admin'

// Setup authentication
CredentialsProvider provider = new BasicCredentialsProvider()
UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(USER, PASSWORD)
provider.setCredentials(AuthScope.ANY, credentials)
HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build()

def map = [:]
map["name"] = "Maritime DevCon"
map["address"] = "Fredericton"
map["handle"] = "maritimedevcon"

def jsonBody = new JsonBuilder(map).toString()



def getEventsUrl = BASE_URL + 'events'
URIBuilder builder = new URIBuilder(getEventsUrl);
builder.setParameter("load-details", "true")
HttpGet getRequest = new HttpGet(builder.build());
getRequest.addHeader("content-type", "application/json")
def response = client.execute(getRequest)
def bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
def jsonResponse = bufferedReader.getText()
def slurper = new JsonSlurper()
def eventListing = slurper.parseText(jsonResponse)

eventListing.each {event ->
    def eventType = event.getAt("xnat-type")
    def eventJson = new JsonBuilder(event).toString()
    println(event['type'])

}



def loggingActionForMRScansSubscription = "{\n" +
        "    \"name\": \"MR Scan Archive on ABC123\",\n" +
        "    \"active\": true,\n" +
        "    \"action-key\": \"org.nrg.xnat.eventservice.actions.EventServiceLoggingAction:org.nrg.xnat.eventservice.actions.EventServiceLoggingAction\",\n" +
        "    \"event-filter\": {\n" +
        "        \"event-type\": \"org.nrg.xnat.eventservice.events.ScanEvent\",\n" +
        "    \"project-ids\": [\n" +
        "      \"ABC123\"\n" +
        "    ],\n" +
        "    \"status\": \"CREATED\",\n" +
        "    \"json-path-filter\": \"\$[?(@.xsiType =~ /.*MRScanData/i && @.frames  > 100 && @.scanner-manufacturer =~ /siemens/i && @.scanner-model =~ /TRIOTIM/i )]\"\n" +
        "    },\n" +
        "    \"act-as-event-user\": false\n" +
        "}"

def loggingActionForMRSessionSubscription = "{\n" +
        "    \"name\": \"MR Scan Archive on ABC123\",\n" +
        "    \"active\": true,\n" +
        "    \"action-key\": \"org.nrg.xnat.eventservice.actions.EventServiceLoggingAction:org.nrg.xnat.eventservice.actions.EventServiceLoggingAction\",\n" +
        "    \"event-filter\": {\n" +
        "        \"event-type\": \"org.nrg.xnat.eventservice.events.ScanEvent\",\n" +
        "    \"project-ids\": [\n" +
        "      \"ABC123\"\n" +
        "    ],\n" +
        "    \"status\": \"CREATED\",\n" +
        "    \"json-path-filter\": \"\$[?(@.xsiType =~ /.*MRScanData/i && @.frames  > 100 && @.scanner-manufacturer =~ /siemens/i && @.scanner-model =~ /TRIOTIM/i )]\"\n" +
        "    },\n" +
        "    \"act-as-event-user\": false\n" +
        "}"