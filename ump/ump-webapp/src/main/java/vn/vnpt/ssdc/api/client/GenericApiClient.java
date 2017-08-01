package vn.vnpt.ssdc.api.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import vn.vnpt.ssdc.api.model.SsdcEntity;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;

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
