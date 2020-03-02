package com.megmeet.megmeetreport.util;

public class Result<T> {

    public Result()
    {
        this.HasError = false;
    }

    public T Value;

    public Boolean HasError;

    public String Error;

    public String ErrorDetails;

    public Result FromErrorResult(Result result)
    {
        if (result != null) {
            this.HasError = result.HasError;
            this.Error = result.Error;
            this.ErrorDetails = result.ErrorDetails;
        }

        return this;
    }
}
