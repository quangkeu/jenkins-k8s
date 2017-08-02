package vn.vnpt.ssdc.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Huy Hieu on 12/9/2016.
 */
public abstract class BasePreset implements Serializable {

    public String channel;
    public int weight;
    public String precondition;
    public List<BaseConfiguration> configurations;
    public String schedule;
    public String events;

    public interface BaseConfiguration extends Serializable {

    }
}
