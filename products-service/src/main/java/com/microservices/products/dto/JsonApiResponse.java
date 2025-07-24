package com.microservices.products.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonApiResponse<T> {
    private T data;
    private List<T> dataList;
    private List<JsonApiError> errors;
    
    public JsonApiResponse() {}
    
    public JsonApiResponse(T data) {
        this.data = data;
    }
    
    public JsonApiResponse(List<T> dataList) {
        this.dataList = dataList;
    }
    
    public static <T> JsonApiResponse<T> success(T data) {
        return new JsonApiResponse<>(data);
    }
    
    public static <T> JsonApiResponse<T> successList(List<T> data) {
        return new JsonApiResponse<>(data);
    }
    
    public static <T> JsonApiResponse<T> error(List<JsonApiError> errors) {
        JsonApiResponse<T> response = new JsonApiResponse<>();
        response.setErrors(errors);
        return response;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }

    public List<JsonApiError> getErrors() {
        return errors;
    }

    public void setErrors(List<JsonApiError> errors) {
        this.errors = errors;
    }
}