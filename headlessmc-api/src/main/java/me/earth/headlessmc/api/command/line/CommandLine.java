package me.earth.headlessmc.api.command.line;

import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import me.earth.headlessmc.api.HeadlessMc;
import me.earth.headlessmc.api.command.*;
import me.earth.headlessmc.api.process.InAndOutProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOError;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Everything related to reading commands from the command line.
 */
@Getter
@Setter
@CustomLog
public class CommandLine implements PasswordAware, QuickExitCli, HasCommandContext, CommandLineReader {
    private final InAndOutProvider inAndOutProvider = new InAndOutProvider();
    /**
     * Provides the {@link CommandLineReader}.
     */
    private volatile Supplier<CommandLineReader> commandLineProvider = new DefaultCommandLineProvider(inAndOutProvider);
    /**
     * The initial CommandContext, so that we do not lose it...
     */
    private volatile CommandContext baseContext = EmptyCommandContext.INSTANCE;
    /**
     * The currently active CommandContext which executes commands written on the CommandLine managed by this CommandLineManager.
     */
    private volatile CommandContext commandContext = EmptyCommandContext.INSTANCE;
    /**
     * Accesses the raw line read from the command line.
     * Is generally only meant for testing, should by default always call execute on {@link #getCommandContext()}.
     */
    private volatile Consumer<String> commandConsumer = line -> getCommandContext().execute(line);
    /**
     * The current {@link CommandLineReader} for reading the command line.
     * Might be {@code null}.
     */
    private volatile @Nullable CommandLineReader commandLineReader;

    private volatile boolean quickExitCli;
    private volatile boolean waitingForInput;
    private volatile boolean hidingPasswords;
    private volatile boolean hidingPasswordsSupported;
    private volatile boolean listening;

    /**
     * Constructs a new CommandLine.
     */
    public CommandLine() {
        setHidingPasswordsSupported(inAndOutProvider.getConsole().get() != null);
    }

    @Override
    public void read(HeadlessMc hmc) throws IOError {
        if (this.commandLineReader != null) {
            log.warn("Listen called, but a CommandLineListener already exists!");
        }

        CommandLineReader commandLineReader = commandLineProvider.get();
        this.commandLineReader = commandLineReader;
        listening = true;
        commandLineReader.read(hmc);
    }

    @Override
    public void readAsync(HeadlessMc hmc, ThreadFactory factory) {
        if (this.commandLineReader != null) {
            log.warn("Listen called, but a CommandLineListener already exists!");
        }

        CommandLineReader commandLineReader = commandLineProvider.get();
        this.commandLineReader = commandLineReader;
        listening = true;
        commandLineReader.readAsync(hmc, factory);
    }

    @Override
    public void open(HeadlessMc hmc) throws IOException {
        CommandLineReader commandLineReader = getCommandLineReader();
        if (commandLineReader != null) {
            commandLineReader.read(hmc);
            listening = true;
        }
    }

    @Override
    public void close() throws IOException {
        CommandLineReader commandLineReader = getCommandLineReader();
        if (commandLineReader != null) {
            listening = false;
            commandLineReader.close();
        }
    }

    private static final class EmptyCommandContext implements CommandContext {
        private static final EmptyCommandContext INSTANCE = new EmptyCommandContext();

        @Override
        public void execute(String command) {
            log.warn("Did you forge to set the CommandContext? EmptyCommandContext: " + command);
        }

        @NotNull
        @Override
        public Iterator<Command> iterator() {
            return Collections.emptyIterator();
        }
    }

}