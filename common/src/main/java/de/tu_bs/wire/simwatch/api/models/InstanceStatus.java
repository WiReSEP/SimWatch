package de.tu_bs.wire.simwatch.api.models;

public class InstanceStatus {
    private Instance.Status status;
    private Instance.Error error;

    public InstanceStatus(Instance.Status status, Instance.Error error) {
        this.status = status;
        this.error = error;
    }

    public Instance.Status getStatus() {
        return status;
    }

    public void setStatus(Instance.Status status) {
        this.status = status;
    }

    public Instance.Error getError() {
        return error;
    }

    public void setError(Instance.Error error) {
        this.error = error;
    }
}
