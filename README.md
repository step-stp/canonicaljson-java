# Java Canonicaljson


Java library for producing JSON in canonical format as specified by [https://gibson042.github.io/canonicaljson-spec/](https://gibson042.github.io/canonicaljson-spec/). The provided interface matches that of native JSON object.

## Installation

### Maven
```bash
com.stratumn.canonicaljson
```

## Usage

```python
import com.stratumn.canonicaljson;

String result = CanonicalJson.stringify(CanonicalJson.parse("{ \"a\": 12 }"));
```


## Development
Integration tests are located in [canonicaljson-spec](https://gibson042.github.io/canonicaljson-spec/)  .
