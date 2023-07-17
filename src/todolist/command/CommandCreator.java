package todolist.command;

import java.util.ArrayList;
import java.util.List;

public class CommandCreator {
    private static List<String> getCommandArguments(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == ' ') {
                tokens.add(sb.toString());
                sb.delete(0, sb.length());
            } else if ( c != ' ') {
                sb.append(c);
            }
        }
        //lets not forget about last token that doesn't have space after it
        tokens.add(sb.toString());

        return tokens;
    }

    public static Command newCommand(String clientInput) {
        final int command = 0;
        final int beginArguments = 1;
        List<String> tokens = CommandCreator.getCommandArguments(clientInput);
        final int endArguments = tokens.size();

        String[] args = tokens.subList(beginArguments, endArguments).toArray(new String[0]);


        return new Command(tokens.get(command), args);
    }
}