import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
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


// Get all events
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


// Get actions for each event type
def getActionUrl = BASE_URL + 'actionsbyevent'
eventListing.each {event ->
    builder = new URIBuilder(getActionUrl)
    builder.setParameter('event-type', event['type'])
    getRequest = new HttpGet(builder.build());
    getRequest.addHeader("content-type", "application/json")
    response = client.execute(getRequest)
    bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
    jsonResponse = bufferedReader.getText()
    slurper = new JsonSlurper()
    actionListing = slurper.parseText(jsonResponse)
    println("Actions for " + event['type'])
    actionListing.each { action ->
        println(action.toString())
    }
}

def loggingActionForMRScansSubscription = "{" +
        "    \"name\": \"MR Scan Archive on ABC123\"" +
        "    \"active\": true," +
        "    \"action-key\": \"org.nrg.xnat.eventservice.actions.EventServiceLoggingAction:org.nrg.xnat.eventservice.actions.EventServiceLoggingAction\"," +
        "    \"event-filter\": {" +
        "        \"event-type\": \"org.nrg.xnat.eventservice.events.ScanEvent\"," +
        "    \"project-ids\": [" +
        "      \"ABC123\"" +
        "    ]," +
        "    \"status\": \"CREATED\"," +
        "    \"json-path-filter\": \"\$[?(@.xsiType =~ /.*MRScanData/i && @.frames  > 100 && @.scanner-manufacturer =~ /siemens/i && @.scanner-model =~ /TRIOTIM/i )]\"" +
        "    }," +
        "    \"act-as-event-user\": false" +
        "}"

def loggingActionForMRSessionSubscription = "{" +
        "    \"name\": \"MR Scan Archive on ABC123\"," +
        "    \"active\": true," +
        "    \"action-key\": \"org.nrg.xnat.eventservice.actions.EventServiceLoggingAction:org.nrg.xnat.eventservice.actions.EventServiceLoggingAction\"," +
        "    \"event-filter\": {" +
        "        \"event-type\": \"org.nrg.xnat.eventservice.events.ScanEvent\"," +
        "    \"project-ids\": [" +
        "      \"ABC123\"" +
        "    ]," +
        "    \"status\": \"CREATED\"," +
        "    \"json-path-filter\": \"\$[?(@.xsiType =~ /.*MRScanData/i && @.frames  > 100 && @.scanner-manufacturer =~ /siemens/i && @.scanner-model =~ /TRIOTIM/i )]\"" +
        "    }," +
        "    \"act-as-event-user\": false" +
        "}"


def createSubscriptionBuilder = new HTTPBuilder(BASE_URL)
def newItemId
createSubscriptionBuilder.request(Method.POST, ContentType.JSON){
    req->
        req.uri.path = 'subscription'
        req.uri.query = [format:'json']
        req.body = loggingActionForMRScansSubscription

        req.response.success { resp, json->
            assert resp.status == 201
            newItemId = json.id
        }

        req.response.failure = { resp ->
            throw new Exception("Failed to create new subscription at " + req.uri.toString())
        }
}

println "New subscription ID = " + newItemId.toString()

