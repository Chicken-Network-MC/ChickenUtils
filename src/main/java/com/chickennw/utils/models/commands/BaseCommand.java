package com.chickennw.utils.models.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public abstract class BaseCommand {

    private final String command;

    private final List<String> alias;
}
