package tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public abstract class CommandLineTool {
    String errorMessage = "";

    protected static String arg(String argument) {
        return "-" + argument;
    }

    public void printHelp() {
        System.out.println();
        System.out.println(getToolDescription());
        System.out.println();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(getUsageString(), getCommandlineOptions());
    }

    protected CommandLine parseCommandLine(String[] args) throws ParseException {
        CommandLineParser parser = new GnuParser(); // replace with BasicParser when Apache commons-cli is released
        return parser.parse(getCommandlineOptions(), args);
    }

    public boolean isInteger(String value, int minValue, int maxValue) {
        try {
            int val = Integer.parseInt(value);
            return val >= minValue && val <= maxValue;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public int parseInt(String value) {
        return Integer.parseInt(value);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public abstract String getUsageString();

    public abstract String getToolDescription();

    public abstract Options getCommandlineOptions();

    public abstract void run(CommandLine cmd);

    //public abstract String getStatusMessage(boolean runStatus);

}
