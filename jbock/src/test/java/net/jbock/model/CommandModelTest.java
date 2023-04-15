package net.jbock.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandModelTest {

    @Test
    void testToBuilder() {
        Option option1 = Option.nullary().withParamLabel("v").withNames(List.of("-v")).build();
        Option option2 = Option.nullary().withParamLabel("p").withNames(List.of("-p")).build();
        CommandModel model1 = CommandModel.builder()
                .addOption(option1)
                .addOption(option2)
                .build();
        assertEquals(2, model1.options().size());
        CommandModel model2 = model1.toBuilder().withOptions(List.of(option1)).build();
        assertEquals(1, model2.options().size());
    }
}