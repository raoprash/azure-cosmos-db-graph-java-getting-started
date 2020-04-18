package GetStarted;

import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.driver.exception.ResponseException;

import java.util.concurrent.ExecutionException;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.tinkerpop.gremlin.driver.Client;

public class GraphApp {

    public static void main(String args[]) throws ExecutionException, InterruptedException{

        if(args.length < 1){
            System.out.println("At least one argument expected");
            return;
        }

        String query = args[0];
        System.out.println("Input Query : "+ query);
        
        Cluster cluster;
        Client client;

        try{
            cluster =  Cluster.build(new File("src/remote.yaml")).create();
            client = cluster.connect();
        } catch (FileNotFoundException fe){
            System.out.println("Connection configuration yaml file not found");
            fe.printStackTrace();
            return;
        }

        System.out.println("Submitting Query : " + query);
        ResultSet results = client.submit(query);

        CompletableFuture<List<Result>> completableFutureResult;
        CompletableFuture<Map<String,Object>> completableFutureStatusProperties;
        List<Result> resultList;
        Map<String,Object> statusProperties;

        try{
            completableFutureResult = results.all();
            completableFutureStatusProperties = results.statusAttributes();
            resultList = completableFutureResult.get();
            statusProperties = completableFutureStatusProperties.get();
            System.out.println("Result Count:" + resultList.size());
            for(Result result : resultList){
                System.out.println("\nQuery Result:\n\t" + result.toString());
                
            }
    
            System.out.println("Status:" + statusProperties.get("x-ms-status-code").toString());
            System.out.println("Total Charge: " + statusProperties.get("x-ms-total-request-charge").toString());
        }catch(ExecutionException | InterruptedException e){
            e.printStackTrace();
            System.exit(1);
        }catch(Exception e){
            e.printStackTrace();
            ResponseException re = (ResponseException)e.getCause();

            System.out.println("Status Code: " + re.getStatusAttributes().get().get("x-ms-status-code"));
            System.out.println("SubStatus code:" + re.getStatusAttributes().get().get("x-ms-substatus-code"));
            System.out.println("Retry After (ms):" + re.getStatusAttributes().get().get("x-ms-retry-after"));
            System.out.println("Request Charge:" + re.getStatusAttributes().get().get("x-ms-total-request-charge"));
            System.out.println("ActivityId: " + re.getStatusAttributes().get().get("x-ms-activity-id"));
            throw(e);
        }

        
    
        cluster.close();
        System.exit(0);
    }
    

}