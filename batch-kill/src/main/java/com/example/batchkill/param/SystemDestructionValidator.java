package com.example.batchkill.param;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

public class SystemDestructionValidator implements JobParametersValidator {

    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        if (parameters == null) {
            throw new JobParametersInvalidException("파라미터 NULL");
        }

        Long destructionPower = parameters.getLong("destructionPower");

        if (destructionPower == null) {
            throw new JobParametersInvalidException("destructionPower 파라미터 필수값.");
        }

        if (destructionPower > 9) {
            throw new JobParametersInvalidException("파괴력 수준 허용치 초과");
        }
    }
}
