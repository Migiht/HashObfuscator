public class Main {
    public static void main(String[] args) throws Throwable {
        Replacer replacer = new Replacer("C:\\Users\\Might\\IdeaProjects\\ProGuardTest\\out\\artifacts\\ProGuardTest_jar\\ProGuardTest.jar", "./proMappings.txt");
        replacer.loadInput();
        replacer.findStrings();
        replacer.replaceNames();
        replacer.saveData();
    }
}
