package df.root;

import android.os.Build;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
