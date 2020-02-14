#201bd-md07-elasticsearch
* Set up and configure ELK.  
  * install ElasticSearch container
    ```docker pull elasticsearch:7.5.2 && /
     docker run -d --name elasticsearch --net cda /
     -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:7.5.2```  
  * check ElasticSearch connection:  
    ```curl -X GET sandbox-hdp.hortonworks.com:9200```  
  * run Kibana container:  
    ```docker run -d --name kibana --network cda -p 5601:5601 kibana:tag```  
  * run LogStash container  
 
 * Reconfigure Spark Streaming Job from “Spark Streaming” 
 lesson to publish data into Elastic (change the endpoint from HDFS to Elastic). 
 Make screenshots.   
   * see comments in ElasticDataSaver clazz  
 
 * Run the Kibana, create a dashboard showing hotels categories (i.e. resulting type) from “Spark Streaming” lesson on the line chart with periodic update (each 10 seconds). Describe basic components (diagrams) in a text file. Make screenshot.  
   * see Screenshot 1
   
  