/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package tellu.no.CloudConnector;

import eu.arrowhead.client.common.CertificateBootstrapper;
import eu.arrowhead.client.common.Utility;
import eu.arrowhead.client.common.exception.ArrowheadException;
import eu.arrowhead.client.common.misc.ClientType;
import eu.arrowhead.client.common.misc.TypeSafeProperties;
import eu.arrowhead.client.common.model.ArrowheadService;
import eu.arrowhead.client.common.model.ArrowheadSystem;
import eu.arrowhead.client.common.model.OrchestrationResponse;
import eu.arrowhead.client.common.model.PreferredProvider;
import eu.arrowhead.client.common.model.ServiceRequestForm;
import eu.arrowhead.client.common.model.TemperatureReadout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLContextConfigurator.GenericStoreException;

public class ConsumerMain {

  private static boolean isSecure;
  private static String orchestratorUrl;
  private static TypeSafeProperties props = Utility.getProp();
  private static final String consumerSystemName = props.getProperty("consumer_system_name");
  
  
  private ConsumerMain(String[] args) {
    //Prints the working directory for extra information. Working directory should always contain a config folder with the app.conf file!
    System.out.println("Working directory: " + System.getProperty("user.dir"));

    //Compile the URL for the orchestration request.
    getOrchestratorUrl(args);

    //Start a timer, to measure the speed of the Core Systems and the provider application system.
    long startTime = System.currentTimeMillis();

    //Compile the payload, that needs to be sent to the Orchestrator - THIS METHOD SHOULD BE MODIFIED ACCORDING TO YOUR NEEDS
    ServiceRequestForm srf = compileSRF();

    //Sending the orchestration request and parsing the response
    String providerUrl = sendOrchestrationRequest(srf);

    //Printing out the elapsed time during the orchestration and service consumption
    long endTime = System.currentTimeMillis();
    System.out.println("Orchestration and Service consumption response time: " + Long.toString(endTime - startTime));
    //Show a message dialog with the response from the service provider
    
    
    //Connect to the provider, consuming its service - THIS METHOD SHOULD BE MODIFIED ACCORDING TO YOUR USE CASE
    consumeService(providerUrl);
    
 
  }

  public static void main(String[] args) {
    new ConsumerMain(args);
  }

  //Compiles the payload for the orchestration request
  private ServiceRequestForm compileSRF() {
    /*
      ArrowheadSystem: systemName, (address, port, authenticationInfo)
      Since this Consumer skeleton will not receive HTTP requests (does not provide any services on its own),
      the address, port and authenticationInfo fields can be set to anything.
      SystemName can be an arbitrarily chosen name, which makes sense for the use case.
     */
	  
	String consumer_add = props.getProperty("consumer_add", "localhost");
	int consumer_port = props.getIntProperty("consumer_port", 8080);
    ArrowheadSystem consumer = new ArrowheadSystem(consumerSystemName, consumer_add, consumer_port, "null");

    //You can put any additional metadata you look for in a Service here (key-value pairs)
    Map<String, String> metadata = new HashMap<>();
    metadata.put("secure", "false");
    metadata.put("version", "1.1");
    if (isSecure) {
      //This is a mandatory metadata when using TLS, do not delete it
      metadata.put("security", "token");
    }
    /*
      ArrowheadService: serviceDefinition (name), interfaces, metadata
      Interfaces: supported message formats (e.g. JSON, XML, JSON-SenML), a potential provider has to have at least 1 match,
      so the communication between consumer and provider can be facilitated.
     */
    
    String serviceName = props.getProperty("apis_service_name");
    ArrowheadService service = new ArrowheadService(serviceName, Collections.singleton("json"), metadata);

    //Some of the orchestrationFlags the consumer can use, to influence the orchestration process
    Map<String, Boolean> orchestrationFlags = new HashMap<>();
    //When true, the orchestration store will not be queried for "hard coded" consumer-provider connections
    orchestrationFlags.put("overrideStore", true);
    //When true, the Service Registry will ping every potential provider, to see if they are alive/available on the network
    orchestrationFlags.put("pingProviders", false);
    //When true, the Service Registry will only providers with the same exact metadata map as the consumer
    orchestrationFlags.put("metadataSearch", true);
    //When true, the Orchestrator can turn to the Gatekeeper to initiate interCloud orchestration, if the Local Cloud had no adequate provider
    orchestrationFlags.put("enableInterCloud", true);
    
    boolean triggerInterCloud = props.getBooleanProperty("triggerInterCloud", false);
    orchestrationFlags.put("triggerInterCloud", triggerInterCloud);
    
    
    //PreferProvider
    List<PreferredProvider> providers = new ArrayList<>();
    String apis_module_name = "APIS_" + props.getProperty("apis_module_name");
    String apis_module_add = props.getProperty("apis_module_add");
    int apis_module_port = props.getIntProperty("apis_module_port", 6565);
    ArrowheadSystem apis_system = new ArrowheadSystem(apis_module_name, apis_module_add, apis_module_port, "null");
    
    PreferredProvider provider = new PreferredProvider(apis_system, null);
    providers.add(provider);
    
    orchestrationFlags.put("onlyPreferred", true);

    //Build the complete service request form from the pieces, and return it
    ServiceRequestForm srf = new ServiceRequestForm.Builder(consumer).requestedService(service).orchestrationFlags(orchestrationFlags).preferredProviders(providers).build();
    System.out.println("Service Request payload: " + Utility.toPrettyJson(null, srf));
    return srf;
  }

  private void consumeService(String providerUrl) {
	  
	 String mqttAdd = props.getProperty("mqtt_add");
	 String mqttUser = props.getProperty("mqtt_user");
	 String mqttPass = props.getProperty("mqtt_pass");
	 String mqttTopic =  props.getProperty("mqtt_topic");
	 MQTTClient mqtt_cl = new MQTTClient(mqttAdd, mqttUser, mqttPass);
	 mqtt_cl.consumeAPISService(providerUrl, mqttTopic);
	 
    
  }

   /*
      Methods that should be modified to your use case ↑
   ----------------------------------------------------------------------------------------------------------------------------------
      Methods that do not need to be modified ↓
   */

  //DO NOT MODIFY - Gets the correct URL where the orchestration requests needs to be sent (from app.conf config file + command line argument)
  private void getOrchestratorUrl(String[] args) {
    String orchAddress = props.getProperty("orch_address", "0.0.0.0");
    int orchInsecurePort = props.getIntProperty("orch_insecure_port", 8440);
    int orchSecurePort = props.getIntProperty("orch_secure_port", 8441);

    for (String arg : args) {
      if (arg.equals("-tls")) {
        isSecure = true;
        SSLContextConfigurator sslCon = new SSLContextConfigurator();
        sslCon.setKeyStoreFile(props.getProperty("keystore"));
        sslCon.setKeyStorePass(props.getProperty("keystorepass"));
        sslCon.setKeyPass(props.getProperty("keypass"));
        sslCon.setTrustStoreFile(props.getProperty("truststore"));
        sslCon.setTrustStorePass(props.getProperty("truststorepass"));

        try {
          SSLContext sslContext = sslCon.createSSLContext(true);
          Utility.setSSLContext(sslContext);
        } catch (GenericStoreException e) {
          System.out.println("Provided SSLContext is not valid, moving to certificate bootstrapping.");
          e.printStackTrace();
          sslCon = CertificateBootstrapper.bootstrap(ClientType.CONSUMER, consumerSystemName);
          props = Utility.getProp();
          Utility.setSSLContext(sslCon.createSSLContext(true));
        }
        break;
      }
    }

    if (isSecure) {
      Utility.checkProperties(props.stringPropertyNames(), ClientType.CONSUMER.getSecureMandatoryFields());
      orchestratorUrl = Utility.getUri(orchAddress, orchSecurePort, "orchestrator/orchestration", true, false);
    } else {
      orchestratorUrl = Utility.getUri(orchAddress, orchInsecurePort, "orchestrator/orchestration", false, false);
    }
  }

  /* NO NEED TO MODIFY (for basic functionality)
     Sends the orchestration request to the Orchestrator, and compiles the URL for the first provider received from the OrchestrationResponse */
  private String sendOrchestrationRequest(ServiceRequestForm srf) {
    //Sending a POST request to the orchestrator (URL, method, payload)
    Response postResponse = Utility.sendRequest(orchestratorUrl, "POST", srf);
    //Parsing the orchestrator response
    OrchestrationResponse orchResponse = postResponse.readEntity(OrchestrationResponse.class);
    System.out.println("Orchestration Response payload: " + Utility.toPrettyJson(null, orchResponse));
    if (orchResponse.getResponse().isEmpty()) {
      throw new ArrowheadException("Orchestrator returned with 0 Orchestration Forms!");
    }

    //Getting the first provider from the response
    ArrowheadSystem provider = orchResponse.getResponse().get(0).getProvider();
    String serviceURI = orchResponse.getResponse().get(0).getServiceURI();
    //Compiling the URL for the provider
    UriBuilder ub = UriBuilder.fromPath("").host(provider.getAddress()).scheme("http");
    if (serviceURI != null) {
      ub.path(serviceURI);
    }
    if (provider.getPort() != null && provider.getPort() > 0) {
      ub.port(provider.getPort());
    }
    if (orchResponse.getResponse().get(0).getService().getServiceMetadata().containsKey("security")) {
      ub.scheme("https");
      ub.queryParam("token", orchResponse.getResponse().get(0).getAuthorizationToken());
      ub.queryParam("signature", orchResponse.getResponse().get(0).getSignature());
    }
    System.out.println("Received provider system URL: " + ub.toString());
    return ub.toString();
  }

}
