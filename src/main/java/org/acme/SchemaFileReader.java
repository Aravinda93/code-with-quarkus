package org.acme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class SchemaFileReader implements OASFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        try {
            Components defaultComponents = OASFactory.createComponents();
            if (openAPI.getComponents() == null) {
                openAPI.setComponents(defaultComponents);
            }

            generateExamples().forEach(openAPI.getComponents()::addExample);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    Map<String, Example> generateExamples() throws Exception {
        final Map<String, Example> examples = new LinkedHashMap<>();

        //Get the remote files from the GitHub
        getRemoteFiles(examples);

        return examples;
    }

    private void getRemoteFiles(final Map<String, Example> examples) throws Exception {

        //Read the GitHub files
        final String inputURL = "https://api.github.com/repos/Aravinda93/code-with-quarkus/contents/src/main/resources/jsonFiles";
        final CloseableHttpResponse folderResponse = httpClient.execute(new HttpGet(inputURL));
        final String responseBody = EntityUtils.toString(folderResponse.getEntity(), StandardCharsets.UTF_8);

        //If the API request provides valid response with content type JSON then get the links to files in that folder
        if (folderResponse.getStatusLine().getStatusCode() == 200 && ContentType.get(folderResponse.getEntity()).toString().equalsIgnoreCase("application/json; charset=utf-8")) {
            //Check if the response contains the valid files link or the link itself is corresponding to file contents
            try {
                final JSONArray jsonArray = new JSONArray(responseBody);


                jsonArray.forEach(item -> {
                    final JSONObject obj = (JSONObject) item;

                    if (obj.getString("download_url").contains(".json")) {
                        //Make request to each file in the GitHub folder and obtain its contents
                        try {
                            final CloseableHttpResponse fileResponse = httpClient.execute(new HttpGet(obj.getString("download_url")));
                            //If the response code is 200 then add the contents to Example
                            if (fileResponse.getStatusLine().getStatusCode() == 200) {
                                final String fileResponseBody = EntityUtils.toString(fileResponse.getEntity(), StandardCharsets.UTF_8);
                                System.out.println(obj.getString("name"));
                                examples.put(obj.getString("name"), OASFactory.createExample().value(objectMapper.readValue(fileResponseBody, ObjectNode.class)));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                throw new Exception("Link corresponds to file : " + e.getMessage());
            }
        }
    }
}
