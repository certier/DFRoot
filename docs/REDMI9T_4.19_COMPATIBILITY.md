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

The exact source revision for MIUI 14.0.3.0 (SJQMIXM) has not been identified.

Two public source candidates were inspected:

- `javashin/android_kernel_xiaomi_bengal`, main at
  `29655da8082e7d68f1b61191cb337a06c5f70c69`, has a 4.19.157 Makefile and
  `arch/arm64/configs/vendor/bengal-perf_defconfig`. Its defconfig sets
  `CONFIG_LOCALVERSION="-perf"`, `CONFIG_KALLSYMS_ALL=y`,
  `CONFIG_MODVERSIONS=y`, and `CONFIG_MODULE_SIG_FORCE=y`. Its visible
  source head is dated 2021-12-07, so it is a candidate baseline, not proof of
  the 2023 stock build's exact source or module ABI.
- `Elchanz3/kernel-for-lime`, branch `sapphire`, has a 4.19.157 Makefile
  and a Bengal perf defconfig. That configuration identifies itself as
  `-AOSPA/sapphire`, so it is a custom-ROM kernel source, not a stock MIUI
  match.

The suffix `g1a6087bc0325` is present in a public kernel repository as the
value of a `localversion` file. The commit only changes the local-version
label, and that repository's Makefile reports Linux 4.19.191. It is not a
source match for this phone's 4.19.157 kernel.

The Xiaomi public kernel-source index lists `lime-q-oss` for Android Q devices
(Redmi Note 9 4G / POCO M3), not a verified Android 12 MIUI 14 source match for
the supplied fingerprint. Community Bengal/Lime trees must not be assumed to
match this specific stock kernel.

## DirtyFrag / CVE-2026-43284 source audit

The earlier statement that CVE-2026-43284 was introduced only in Linux 6.5
was too broad and has been corrected. The current Linux CVE record tracks the
ESP issue to code introduced in 4.11. A 4.19-specific CIP backport is recorded
at 4.19.325-cip133, so a 4.19 kernel cannot be dismissed based only on its
major/minor version.

The 4.19 backport is especially relevant because it adapts the fix to the
legacy `ip_append_page()` path. Its patch:
- marks page fragments shared in IPv4 `ip_append_page()`;
- makes IPv4 ESP input avoid its in-place fast path when
  `skb_has_shared_frag()` is true;
- applies the corresponding shared-fragment check to IPv6 ESP input.

The inspected 4.19.157 community candidates contain the legacy
`ip_append_page()` and ESP no-COW fast paths, but do not show those three
backport changes. This is a source-level warning only: neither candidate is
the verified source for the user's stock MIUI kernel, and no runtime
vulnerability test has been performed.

CVE-2026-43500 is a distinct RxRPC path. DFRoot's present implementation uses
XFRM/IPsec ESP and does not automatically exercise RxRPC.

## Module ABI and signing constraints

The candidate Bengal perf defconfig enables loadable modules, module
versioning and forced module signatures. A module that merely compiles for
generic 4.19.157 is therefore not sufficient: the running kernel's exact
configuration, symbol versions, signing/trust policy and vendor symbol layout
must match. These values have not been confirmed for the running MIUI kernel.

## Current result

No exploit test, page-cache write, kernel-module load or root attempt has been
performed on the device. No 4.19 module has been built or validated. Root
support remains unconfirmed and the app continues to block the unsupported
launch path.

References:
- Current Linux CVE record:
  https://kernel.googlesource.com/pub/scm/linux/security/vulns/+/09b74d1628ea888153f0194df38a4c2f3a80ba5f/cve/published/2026/CVE-2026-43284.mbox
- 4.19.325-cip133 fix version and patch ID:
  https://kernelscan.io/cve/CVE-2026-43284
- 4.19 backport commit:
  https://github.com/bengal-upstream/kernel_xiaomi_bengal/commit/d985920ced659354abba0d845c9ccadda6ef80da
- 4.19.157 Bengal candidate:
  https://github.com/javashin/android_kernel_xiaomi_bengal
- Lime/Bengal custom-ROM candidate:
  https://github.com/Elchanz3/kernel-for-lime
- Xiaomi public kernel-source index:
  https://github.com/MiCode/Xiaomi_Kernel_OpenSource
- DFRoot:
  https://github.com/certier/DFRoot

## Changes in this branch

- Lowers minSdk from 32 to 31 for Android 12 / API 31 installation.
- Adds read-only device diagnostics and a best-effort `/proc/config.gz` summary for module-signing and ESP-related kernel options.
- Disables the UI launch action when the kernel family has no embedded LKM.
- Adds a Java-side guard in ExploitRunner before the native helper is started.
- Does not add a 4.19 exploit or kernel module and does not claim Redmi 9T root
  support.

## Evidence required before a genuine 4.19 port

1. Exact source revision used for MIUI 14.0.3.0 (SJQMIXM), including vendor
   patches, not just a similarly named community kernel.
2. Matching generated kernel configuration, symbol-version data and signing
   trust policy.
3. Audit of the stock XFRM/ESP and RxRPC implementations and relevant
   backports.
4. A device-specific module build and independent validation away from the
   user's primary system, with a tested recovery plan.

Until this evidence exists, DFRoot intentionally reports the device as
unsupported rather than attempting an unverified root operation.
