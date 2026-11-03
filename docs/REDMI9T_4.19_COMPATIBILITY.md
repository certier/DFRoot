# Redmi 9T / Linux 4.19.157 compatibility status

## Device evidence

- Model: Xiaomi M2010J19SG (Redmi 9T)
- Codename: lime
- Platform: bengal
- MIUI: Global 14.0.3.0 (SJQMIXM)
- Android: 12, build ID SKQ1.211202.001 (API 31)
- Android security patch: 2023-03-01
- Fingerprint: Redmi/lime_global/lime:12/SKQ1.211202.001/V14.0.3.0.SJQMIXM:user/release-keys
- Kernel: 4.19.157-perf-g1a6087bc0325-dirty

## Existing DFRoot module coverage

The current native image table embeds modules for Android/KMI families 5.10,
5.15, 6.1, 6.6, 6.12 and 6.18. It contains no 4.19 module image and the build
workflow has no 4.19 KMI target. The Android app must therefore not advertise
this phone as supported or start the existing chain on 4.19.

This branch adds a Java-side preflight guard before DFRoot stages assets or
starts the native helper. The UI also disables the launch button when the
running kernel family has no embedded module image. This protects the normal
app path; it is not a claim that a 4.19 port exists.

## Kernel-source matching status

The suffix `g1a6087bc0325` is present in a public kernel repository as the
value of its `localversion` file. That commit only changes the local-version
label, and that repository's Makefile reports Linux 4.19.191. This is a useful
search lead, not proof that its source revision produced this phone's
4.19.157 kernel. Do not use its module ABI or symbols as a match for this
device without further evidence.

The Xiaomi public kernel-source index lists `lime-q-oss` for Android Q devices
(Redmi Note 9 4G / POCO M3), not a verified Android 12 MIUI 14 source match for
the supplied fingerprint. Community bengal/lime trees are also not assumed
to match this specific stock kernel.

## DirtyFrag / CVE status

DFRoot's current exploit uses the XFRM/IPsec ESP path (CVE-2026-43284). The
Linux CVE announcement says the upstream issue was introduced in Linux 6.5.
A stock upstream 4.19 tree therefore does not contain that later-introduced
change. Downstream vendors can backport code, so the version string alone
cannot establish whether the same vulnerable code exists in Xiaomi's kernel.

CVE-2026-43500 is a distinct RxRPC path. Its upstream issue was introduced in
Linux 5.3, so stock upstream 4.19 does not contain that later-introduced
change either. A vendor backport remains possible; additionally, DFRoot's
current ESP implementation does not automatically exploit RxRPC.

No exploit test or module load has been attempted on the device. Root support
is unconfirmed.

References:
- https://lists.openwall.net/linux-cve-announce/2026/05/08/1
- https://lists.openwall.net/linux-cve-announce/2026/05/11/1
- https://github.com/Joe7500/kernel_xiaomi_chime/commit/22dc325737729f8f678304533642f06a669186d7
- https://github.com/Joe7500/kernel_xiaomi_chime
- https://github.com/MiCode/Xiaomi_Kernel_OpenSource
- https://github.com/certier/DFRoot

## Changes in this branch

- Lowers minSdk from 32 to 31 for Android 12 / API 31 installation.
- Adds read-only device and kernel diagnostics.
- Disables the UI launch action when the kernel family has no embedded LKM.
- Adds a Java-side guard in ExploitRunner before the native helper is started.
- Does not add a 4.19 exploit or kernel module and does not claim Redmi 9T root
  support.

## Evidence required before a genuine 4.19 port

1. Exact source revision used for MIUI 14.0.3.0 (SJQMIXM), including vendor
   patches, not just a similarly named community kernel.
2. Matching defconfig and generated kernel configuration.
3. Audit of the vendor XFRM/ESP and RxRPC implementations and relevant
   backports.
4. Kernel module ABI data and build configuration.
5. A separately validated, device-specific module and a recovery plan.

Until this evidence exists, DFRoot intentionally reports the device as
unsupported rather than attempting an unverified root operation.
