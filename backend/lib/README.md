# SAP JCo runtime

SAP JCo is proprietary software and is not committed to this repository.

For SAP RFC execution in the customer service, place the SAP JCo runtime files in this directory:

- `sapjco3.jar`
- Linux native library such as `libsapjco3.so`

Then restart the customer service:

```bash
SERVICES=customer scripts/start-services.sh
```

You can also keep the files elsewhere and point the startup script to that directory:

```bash
SAP_JCO_LIB_DIR=/opt/sapjco SERVICES=customer scripts/start-services.sh
```

If the native library is in a different directory from `sapjco3.jar`, set `SAP_JCO_NATIVE_DIR` as well.
