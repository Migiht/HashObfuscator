package extension;

public class Main {
    public static void main(String[] args) throws Throwable {
        Replacer replacer = new Replacer(args[0], args[1]);
        replacer.loadInput();
        replacer.findStrings();
        replacer.generateConfigOld();
        replacer.saveData();
    }
}
