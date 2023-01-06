# Apache Fineract CN Customer Management [![Docker Cloud Build Status](https://img.shields.io/docker/cloud/build/apache/fineract-cn-customer)](https://hub.docker.com/r/apache/fineract-cn-customer/builds)

This service covers simple functionality around CRM and KYC. [Read more](https://cwiki.apache.org/confluence/display/FINERACT/Fineract+CN+Project+Structure#FineractCNProjectStructure-customer).

## Versioning
The version numbers follow the [Semantic Versioning](http://semver.org/) scheme.

In addition to MAJOR.MINOR.PATCH the following postfixes are used to indicate the development state.

* BUILD-SNAPSHOT - A release currently in development. 
* M - A _milestone_ release include specific sets of functions and are released as soon as the functionality is complete.
* RC - A _release candidate_ is a version with potential to be a final product, considered _code complete_.
* RELEASE - _General availability_ indicates that this release is the best available version and is recommended for all usage.

The versioning layout is {MAJOR}.{MINOR}.{PATCH}-{INDICATOR}[.{PATCH}]. Only milestones and release candidates can  have patch versions. Some examples:

1.2.3.BUILD-SNAPSHOT  
1.3.5.M.1  
1.5.7.RC.2  
2.0.0.RELEASE

## License
See [LICENSE](LICENSE) file.
