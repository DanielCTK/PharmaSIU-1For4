# PharmaSIU — Java/XML package refactor + UI design system

## Goal and invariants

One Android app module `:app`, appId and namespace `com.pharmasiu.mobile`, Java 17 + XML only. Preserve currently functioning RAM demo features, with explicit RX/FEFO/role/shift guards, no claimed backend payments/stock/pharmacist verification. Do not clone upstream UI repos or copy unreviewed code. The UI dependency must be pinned, buildable and licensed. Changes land through the original project.

## Target structure

```text
app/src/main/java/com/pharmasiu/mobile/
  MainActivity.java              # thin composition root/Android lifecycle
  navigation/                     # AppRouter, navigation bottom bar, role screen selection
  core/                           # shared contracts UiHost and state façade; split DemoState domain stores
    state/                         # CustomerState, PosState, AdminState, AccountSession
  data/model/                     # plain business records
  data/repository/                # in-memory demo catalog fixtures
  domain/                         # pure Java rules (FEFO, Rx, status, permissions)
  ui/
    design/                        # PharmaTheme, ViewFactory, surface/button/chip components
    auth/                          # auth controller/form
    customer/
      catalog/                     # home, catalog, product
      orders/                      # cart, Rx, checkout, history, receipt
      account/                     # profile, address, favorites, onboarding
    staff/
      pos/                         # register, cart, shift, receipt, history
      admin/                       # dashboard, catalog, lot, audit, requests, reports
      media/                       # product photo preview (local only)
```

Layout resources use `screen_<role>_<feature>_*.xml`, `item_<feature>_*.xml`, `component_*.xml`; design tokens in values; always replace old layout refs in Java before removing old resources. Java package structure must match folders, **no other Android module**.

## Ownership for parallel agents

- Customer: `ui/customer/**`, customer/catalog/order/account layout XML. Split oversized 500-line classes into controllers/screens/helper classes with explicit interfaces. Do not change state or app router.
- POS: `ui/staff/pos/**` and POS layouts; split shift/register/receipt/history. No core or admin edits.
- Admin: `ui/staff/admin/**`, `ui/staff/media/**`, staff_admin/staff_media layouts; split inventory/catalog/report/requests. No core or POS edits.
- State: `core/DemoState.java`, new `core/state/**`, `data/**`, `domain/**` only; do not break public core contract or logic.
- Design: `ui/design/**`, `res/values/ui_*.xml`, `res/drawable/component_*.xml`. Provide reusable consistent buttons/cards/typography states and example adoption; no feature modifications.
- Prime: MainActivity/navigation, root Gradle/manifest, dependency resolution, resource conventions, integration, test, docs, and assembly; no duplicate edits.

## UI language

Premium Emerald, not generic flat gray Android widgets. Dark forest #073D30 and emerald #064D3A, mint #E6F3E6, lime #B9F48B, cream #F6F7F2, ink #192A24. 4/8dp rhythm, card radius 18–24dp, clear typographic hierarchy (app bar/hero/section/body/metadata), 48dp+ touch targets, outlined text fields, tailored empty/loading/disabled/error states, editorial header and tasteful layered gradients. Avoid inventing customer product photography and do not visually imply payment, Rx approval, or stock holds. Include contentDescription, TalkBack, contrast, 320dp and 390dp no clipping, keyboard-aware layouts, reduce animation for accessibility.

## Third-party dependency

Use Google's official [Material Components for Android](https://github.com/material-components/material-components-android), pinned `com.google.android.material:material:1.14.0` (Apache 2.0) and Material3 Views theme *only after Gradle build confirms resolve*. This Views library is now in maintenance mode; app must stay Java/XML by user requirement. Do not add speculative animation/photo/network dependencies. Native Android Views may coexist, use `MaterialButton`, `TextInputLayout` and shape drawable incrementally.

## Acceptance

Original `:app:clean :app:assembleDebug :app:testDebugUnitTest` passes; 141 Java unit tests baseline plus any new tests. Login/customer/POS/admin smoke on emulator; dynamic routing preserved; appId unchanged. Each agent reports per-file diff/behavior, tests and unverified parity. Avoid declaring every screen fully modernized until visually confirmed.
