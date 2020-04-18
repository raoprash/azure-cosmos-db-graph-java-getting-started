package GetStarted;

import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;

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

            for(Result result : resultList){
                System.out.println("\nQuery Result:\n\t" + result.toString());
            }
    
            System.out.println("Status:" + statusProperties.get("x-ms-status-code").toString());
            System.out.println("Total Charge: " + statusProperties.get("x-ms-total-request-charge").toString());
            
        }catch(ExecutionException | InterruptedException e){
            e.printStackTrace();
            return;
        }catch(Exception e){
            e.printStackTrace();
        }

        
    
        cluster.close();
        System.exit(0);
    }
    

}