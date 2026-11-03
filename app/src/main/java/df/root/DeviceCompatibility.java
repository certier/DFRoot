package df.root;

import android.os.Build;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Read-only compatibility checks for the kernel module images embedded in DFRoot.
 *
 * A kernel-family match is only a packaging prerequisite. It does not prove
 * that DirtyFrag is present, that the vendor kernel is unpatched, or that the
 * module can safely load on a particular device.
 */
final class DeviceCompatibility {
    private static final Set<String> EMBEDDED_KERNEL_FAMILIES =
            new HashSet<>(Arrays.asList("5.10", "5.15", "6.1", "6.6", "6.12", "6.18"));
    private static final String KERNEL_RELEASE =
            System.getProperty("os.version", "unknown");
    private static final String[] KERNEL_CONFIG_KEYS = {
            "CONFIG_MODULES",
            "CONFIG_MODVERSIONS",
            "CONFIG_MODULE_SIG",
            "CONFIG_MODULE_SIG_FORCE",
            "CONFIG_MODULE_SIG_ALL",
            "CONFIG_KALLSYMS",
            "CONFIG_KALLSYMS_ALL",
            "CONFIG_XFRM_USER",
            "CONFIG_INET_ESP",
            "CONFIG_INET6_ESP",
            "CONFIG_SECURITY_SELINUX",
            "CONFIG_SECURITY_SELINUX_DEVELOP",
            "CONFIG_STATIC_USERMODEHELPER",
            "CONFIG_IKCONFIG_PROC"
    };

    private DeviceCompatibility() {}

    static String kernelFamily() {
        String[] fields = KERNEL_RELEASE.split("\\.");
        if (fields.length < 2 || !isDigits(fields[0]) || !isDigits(fields[1])) {
            return "unknown";
        }
        return fields[0] + "." + fields[1];
    }

    private static boolean isDigits(String value) {
        if (value.isEmpty()) return false;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }

    static boolean hasEmbeddedKernelProfile() {
        return EMBEDDED_KERNEL_FAMILIES.contains(kernelFamily());
    }

    /**
     * Read selected, non-secret kernel build options if the kernel exposes
     * /proc/config.gz. This method never writes to the device.
     */
    private static String readKernelConfigReport() {
        Map<String, String> found = new HashMap<>();
        Set<String> wanted = new HashSet<>(Arrays.asList(KERNEL_CONFIG_KEYS));

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream("/proc/config.gz")),
                StandardCharsets.US_ASCII))) {
            String line;
            int totalChars = 0;
            while ((line = reader.readLine()) != null && totalChars < 2_000_000) {
                totalChars += line.length();
                String key = null;
                String value = null;

                if (line.startsWith("CONFIG_")) {
                    int equals = line.indexOf('=');
                    if (equals > 0) {
                        key = line.substring(0, equals);
                        value = line.substring(equals + 1);
                    }
                } else if (line.startsWith("# CONFIG_") && line.endsWith(" is not set")) {
                    key = line.substring(2, line.length() - " is not set".length());
                    value = "n";
                }

                if (key != null && wanted.contains(key)) {
                    found.put(key, value);
                }
            }
        } catch (IOException | SecurityException e) {
            return "Kernel config: /proc/config.gz unavailable (not exposed or not readable)\n";
        }

        StringBuilder out = new StringBuilder();
        out.append("Kernel config source: /proc/config.gz (read-only)\n");
        for (String key : KERNEL_CONFIG_KEYS) {
            out.append("  ").append(key).append(": ")
                    .append(found.containsKey(key) ? found.get(key) : "not listed")
                    .append('\n');
        }
        return out.toString();
    }

    static String buildReport() {
        String family = kernelFamily();
        StringBuilder out = new StringBuilder();
        out.append("DFRoot device diagnostics\n");
        out.append("========================\n");
        out.append("Manufacturer: ").append(Build.MANUFACTURER).append('\n');
        out.append("Model: ").append(Build.MODEL).append('\n');
        out.append("Device: ").append(Build.DEVICE).append('\n');
        out.append("Product: ").append(Build.PRODUCT).append('\n');
        out.append("Hardware: ").append(Build.HARDWARE).append('\n');
        out.append("Android: ").append(Build.VERSION.RELEASE)
                .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        out.append("Security patch: ").append(Build.VERSION.SECURITY_PATCH).append('\n');
        out.append("Kernel: ").append(KERNEL_RELEASE).append('\n');
        out.append("Kernel family: ").append(family).append('\n');
        out.append(readKernelConfigReport());

        if (hasEmbeddedKernelProfile()) {
            out.append("Embedded DFRoot LKM: family match available\n");
            out.append("Status: profile match only; exploit/device compatibility is unverified\n");
        } else {
            out.append("Embedded DFRoot LKM: NONE for this kernel family\n");
            out.append("Status: ROOT LAUNCH DISABLED\n");
            out.append("Reason: no matching kernel module is packaged in this build.\n");
            out.append("No exploit setup will be started by the app.\n");
        }

        out.append("Security patch/build fingerprint: ")
                .append(Build.FINGERPRINT).append('\n');
        return out.toString();
    }
}
