public class ApkBuilderCmd {
    public static void main(String[] args) throws Exception {
        (new com.android.sdklib.build.ApkBuilder(
            args[0], args[1], args[2], args[3], null)).sealApk();
    }
}