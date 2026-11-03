# Redmi 9T / Linux 4.19.157 compatibility status

## Device evidence

- Model: Xiaomi M2010J19SG (Redmi 9T)
- MIUI: Global 14.0.3.0 (SJQMIXM)
- Android: 12, build ID SKQ1.211202.001
- Android security patch shown: 2023-03-01
- Kernel: 4.19.157-perf-g1a6087bc0325-dirty

The screenshot does not provide the complete build fingerprint, device
codename, kernel configuration, source revision, or module symbol/version data.

## Existing DFRoot module coverage

The current native image table embeds modules for kernel families 5.10, 5.15,
6.1, 6.6, 6.12 and 6.18. It contains no 4.19 module image and the build workflow
has no 4.19 KMI target. The Android app therefore must not advertise this phone
as supported or start the existing chain on 4.19.

This branch adds a Java-side preflight guard before DFRoot stages assets or
starts the native helper. The UI also disables the launch button when the
running kernel family has no embedded module image. This protects the normal
app and boot-receiver entry points; it is not a claim that a 4.19 port exists.

## CVE-2026-43284 status

DFRoot identifies its exploit primitive as CVE-2026-43284 (DirtyFrag,
XFRM/ESP). The Linux CVE announcement says the upstream issue was introduced
by commit 7da0dde68486b2d5bd7c689a9b327b77efecdfd0 in Linux 6.5. A stock
upstream 4.19 tree therefore does not contain that later-introduced change.
Vendor trees can contain backports or custom modifications, so the exact
Xiaomi source tree must be checked before making a device-specific conclusion.

CVE-2026-43500 (RxRPC) is a distinct code path and is not automatically usable
by DFRoot's current ESP-based implementation. Kernel version alone cannot
establish whether either issue is present in a downstream vendor build.

References:
- https://lists.openwall.net/linux-cve-announce/2026/05/08/1
- https://kb.cert.org/vuls/id/980487
- https://github.com/certier/DFRoot

## Changes in this branch

- Lowers minSdk from 32 to 31 so standard Android 12 / API 31 can install the
  diagnostics build (pending CI confirmation).
- Adds read-only device and kernel diagnostics.
- Disables the UI launch action when the kernel family has no embedded LKM.
- Adds a Java-side guard in ExploitRunner before the native helper is started.
- Does not add a 4.19 exploit or kernel module and does not claim Redmi 9T root
  support.

## Evidence required before a genuine 4.19 port

1. Exact getprop ro.product.device, ro.board.platform, build fingerprint and
   uname -a output from the phone.
2. Matching Xiaomi kernel source revision and defconfig.
3. Audit of the vendor XFRM/ESP implementation and relevant backports.
4. Kernel configuration and module ABI information.
5. A separately validated, device-specific module and recovery plan.

Until this evidence exists, DFRoot intentionally reports the device as
unsupported instead of attempting an unverified root operation.
