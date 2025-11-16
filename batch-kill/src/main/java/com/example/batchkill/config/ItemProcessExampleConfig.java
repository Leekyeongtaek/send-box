package com.example.batchkill.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

@Slf4j
public class ItemProcessExampleConfig {

    public static class CommandValidator implements Validator<Command> {

        @Override
        public void validate(Command value) throws ValidationException {
            if (value.getCommandText().contains("rm")) {
                throw new ValidationException("command text contains 'rm'");
            }
        }
    }

    //@Bean
    public ItemProcessor<Command, Command> commandItemProcessor() {
        ValidatingItemProcessor<Command> processor = new ValidatingItemProcessor<>(new CommandValidator());
        processor.setFilter(true); //해당 데이터가 필터링되도록 null이 반환된다
        //processor.setFilter(false); // 상위로 예외 전파
        return processor;
    }

    public static class ExecutionerProcessor implements ItemProcessor<Command, Command> {

        @Override
        public Command process(Command item) throws Exception {

            if (item.getCommandText().contains("rm -rf /")) {
                return null;
            }

            //...
            return item;
        }
    }

    @Data
    public static class Command {
        private String commandText;
    }
}
