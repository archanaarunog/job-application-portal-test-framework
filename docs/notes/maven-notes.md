# Maven Commands for Test Execution

## Basic Commands

### Clean the project (removes target directory)
```bash
mvn clean
```

### Compile the project
```bash
mvn compile
```

### Run all tests
```bash
mvn test
```

### Clean and run all tests
```bash
mvn clean test
```

## Running Specific Tests

### Run a specific test class
```bash
mvn -Dtest=ClassName test
```
Example:
```bash
mvn -Dtest=LoginTest test
```

### Run a specific test method
```bash
mvn -Dtest=ClassName#MethodName test
```
Example:
```bash
mvn -Dtest=LoginTest#validLogin test
```

### Run multiple specific test classes
```bash
mvn -Dtest=ClassName1,ClassName2 test
```

### Run tests by pattern
```bash
mvn -Dtest=*Test test
```

## Running Tests with Properties

### Run tests with environment and browser configuration
```bash
mvn -Denv=dev -Dbrowser=chrome test
```

### Run specific test with properties
```bash
mvn -Denv=dev -Dbrowser=chrome -Dtest=LoginTest#validLogin test
```

## Test Groups (if using TestNG groups)

### Run tests by group
```bash
mvn -Dgroups=smoke test
```

### Run tests excluding certain groups
```bash
mvn -DexcludedGroups=negative test
```

## Generating Reports

### Generate Surefire test reports
```bash
mvn surefire-report:report
```

### Generate reports after running tests
```bash
mvn clean test surefire-report:report
```

## Debugging Tests

### Run tests in debug mode
```bash
mvn -Dmaven.surefire.debug test
```

### Run tests with verbose output
```bash
mvn -Dtest=LoginTest -Dsurefire.printSummary=true test
```

## Common Combinations

### Clean, compile, and run specific test
```bash
mvn clean compile -Dtest=LoginTest#validLogin test
```

### Run all tests with properties and generate reports
```bash
mvn clean -Denv=dev -Dbrowser=chrome test surefire-report:report
```

### Run smoke tests only
```bash
mvn -Denv=dev -Dbrowser=chrome -Dgroups=smoke test
```

## Notes

- Replace `ClassName` with your actual test class name (without .java extension)
- Replace `MethodName` with your actual test method name
- The `-Denv` and `-Dbrowser` properties are specific to this project's configuration
- Test results are stored in `target/surefire-reports/`
- Screenshots for failed tests are saved in `target/screenshots/`
