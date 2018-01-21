package person.pushkar.atc.client;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import person.pushkar.atc.model.Flight;
import person.pushkar.atc.model.FlightLedger;

/**
 * Command line client for the ATC 
 *
 */
public class CmdLineClient 
{
    private static final String BASE_URI = "http://localhost:8082/spring-jersey/api/v1/atc/";
    
    private static enum Command {
    	TAKE_OFF("takeOff", "takeOff/airplane/"),
    	LAND("land", "landing/airplane/"),
    	STATUS("status", "status/"),
    	COMMISSION_RUNWAY("commissonRunway", "runway/commission/"),
    	DECOMMISSION_RUNWAY("commissonRunway", "runway/decommission/");
    	private String commandText;
    	private String uri;
    	
    	private Command(String commandText, String uri) {
    		this.commandText = commandText;
    		this.uri = uri;
    	}
    	
    	public static Command findByCommandText(String commandText) {
    		for (Command command : Command.values()) {
				if(command.commandText.equals(commandText)) {
					return command;
				}
			}
    		
    		return null;
    	}
    	
    }

    public static void main( String[] args )
    {
    	System.out.println("Welcome to ATC client. Press Ctrl + C to exit");
    	Scanner in = new Scanner(System.in);
    	
        Client client = ClientBuilder.newClient();

    	try {
	        while(true) {
	        	System.out.print("atc>");
	        	String input = in.nextLine();
	        	
	        	String[] tokens = input.trim().split(" ");
	        	
	        	Command command = Command.findByCommandText(tokens[0]);
	        	
	        	if(command == null) {
	        		System.out.println("Invalid command");
	        	} else {
		        	switch(command) {
		        	case LAND:
		        	case TAKE_OFF:
		        	case COMMISSION_RUNWAY:
		        	case DECOMMISSION_RUNWAY:
		        		client.target(BASE_URI + command.uri).path(tokens[1])
		        			.request(MediaType.APPLICATION_JSON).post(Entity.json(""));
		        		break; 
		        		
		        	case STATUS:
		        		FlightLedger ledger = 
		        				client.target(BASE_URI + command.uri).request(MediaType.APPLICATION_JSON)
		        				  .get(FlightLedger.class);
		        		
		        		System.out.println("Time: " + ledger.getTime() + " sec");
		        		System.out.println("Inflight for takeoff: " + getFormattedFlightMessage(ledger.getInFlightTakeOff()));
		        		System.out.println("Waiting for takeoff: " + getFormattedFlightMessage(ledger.getWaitingForTakeOff()));
		        		System.out.println("Inflight for landing: " + getFormattedFlightMessage(ledger.getInFlightLanding()));
		        		System.out.println("Waiting for landing: " + getFormattedFlightMessage(ledger.getWaitingForLanding()));
		        		System.out.println("Successful takeoffs: " + getFormattedFlightMessage(ledger.getSuccessfullyTookOff()));
		        		System.out.println("Successful landings: " + getFormattedFlightMessage(ledger.getSuccessfullyLanded()));
		        		break;
		        	}
	        	}
	        }
    	} catch (Throwable e) {
    		System.out.println("An error occured : " + e.getMessage());
    	}
    }
    
    private static String getFormattedFlightMessage(List<Flight> flights) {
    	if(flights == null || flights.isEmpty()) {
    		return "none";
    	} 
    	
    	return flights.stream().map(Flight::getId).collect(Collectors.joining(", "));
    }
}
