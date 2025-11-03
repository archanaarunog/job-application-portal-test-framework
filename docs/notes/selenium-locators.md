# Selenium Selectors Cheatsheet — XPath & CSS (table reference)

Quick reference table: pattern → XPath example | CSS example | note

| Pattern | XPath example | CSS example | Note |
|---|---|---|---|
| ID exact | `//input[@id='email']` | `input#email` | Prefer `id` when stable and unique. |
| attribute (name) | `//input[@name='email']` | `input[name='email']` | Use `name` when `id` missing. |
| data-* attribute | `//*[@data-test='login-email']` | `[data-test='login-email']` | Preferred stable selector for tests. |
| contains attribute | `//button[contains(@class,'submit')]` | `button[class*='submit']` | Partial match, useful for dynamic classes. |
| contains text | `//*[contains(normalize-space(.),'Sign in')]` | n/a (use XPath) | Good for buttons/labels where text is unique. |
| exact text | `//div[normalize-space(text())='Login successful!']` | n/a | Exact match; whitespace-normalized. |
| starts-with attribute | `//div[starts-with(@id,'user_')]` | `div[id^='user_']` | CSS `^=` for starts-with. |
| ends-with attribute | n/a (XPath: use substring()) | `a[href$='.pdf']` | CSS `'$='` supported; XPath less convenient. |
| descendant (any depth) | `//form//input[@name='email']` | `form input[name='email']` | Use for inputs inside a specific form. |
| direct child | `//ul/li` | `ul > li` | Immediate child selector. |
| sibling (following) | `//label[text()='Email']/following-sibling::input[1]` | `label + input` (adjacent) | XPath for complex sibling traversal. |
| nth element | `(//button[@class='btn'])[2]` | `ul li:nth-child(3)` | XPath is 1-based; CSS `nth-child` is handy. |
| element with multiple classes | `//*[contains(@class,'classA') and contains(@class,'classB')]` | `.classA.classB` | CSS concise for multiple classes. |
| attribute starts-with (CSS) | `//tag[starts-with(@data-id,'x')]` | `[data-id^='x']` | Prefer CSS when possible for speed. |
| transient text (contains) | `//*[contains(normalize-space(.),'Login successful! Redirecting')]` | n/a | Good for short-lived toasts; use waits. |
| inside specific form | `//form[@id='loginForm']//input[@name='email']` | `form#loginForm input[name='email']` | Scoped selector reduces collisions. |
| visible text button | `//button[normalize-space()='Sign in']` | n/a | Use when button text is stable. |
| link by partial text | `//a[contains(normalize-space(.),'Forgot password')]` | n/a | XPath required for partial text matching in content.


## Usage examples in Java
- CSS: `By.cssSelector("input[name='email']")`
- XPath: `By.xpath("//button[normalize-space()='Sign in']")`

## Quick wait pattern (copy-paste)
```text
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))).click();
```

## Notes
- Prefer `data-*`, `id`, `name` for stability. Avoid absolute XPaths from the root (`/html/...`).
- For transient messages (toasts) use `visibilityOfElementLocated` then `invisibilityOfElementLocated` to detect appear→disappear.
- If an element is inside an iframe or shadow DOM, switch context or use JS to access it.

Keep this table handy when authoring page objects; copy the XPath or CSS example into your `By` definitions depending on which strategy you prefer.
