package vn.vnpt.ssdc.api.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.UmpApiResponse;
import vn.vnpt.ssdc.api.model.SsdcEntity;

import java.io.Serializable;

/**
 * Generic CRUD api client
 *
 * Created by vietnq on 11/1/16.
 */
public class GenericApiClient<ID extends Serializable,T extends SsdcEntity<ID>> {

    protected RestTemplate restTemplate;
    protected String endpointUrl;
    protected Class<T> entityClass;

    public T create(T entity) {
//        ResponseEntity responseEntity = restTemplate.postForEntity(endpointUrl,entity,Object.class);
//        if(responseEntity.getStatusCodeValue() == 200) {
//            return (T)responseEntity.getBody();
//        } else {
//            UmpApiResponse umpApiResponse = (UmpApiResponse)responseEntity.getBody();
//            throw new RuntimeException(umpApiResponse.getErrorMessage());
//        }

        return restTemplate.postForObject(endpointUrl,entity,entityClass);
    }

    public T get(ID id) {
        return restTemplate.getForObject(endpointUrl + "/" + id, entityClass);
    }

    public void update(ID id, T entity) {
        restTemplate.put(endpointUrl + "/" + id,entity);
    }

    public void delete(ID id) {
        restTemplate.delete(endpointUrl + "/" + id);
    }
}
