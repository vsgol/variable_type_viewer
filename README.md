# Variable Type Viewer

A [plugin](build/distributions/variable_type_viewer-1.0-SNAPSHOT.zip) for **PyCharm** that displays the type of the Python variable or expression currently under the caret (cursor) — directly in the status bar.

You can install it in PyCharm via **Settings → Plugins → Install plugin from disk**.

## Features

- Shows the type of the variable, literal, or function under the caret.
- Supports:
    - Variables in assignments (`foo = 123`)
    - Function parameters (`def f(x):`)
    - Object attributes (`self.attr`)
    - Some literals: `str`, `int`, `float`, `bool`, `None`
    - Function definitions
- Ignores keywords, punctuation, operators, whitespace, and comments.
- Avoids redundant updates while moving the caret within the same word.

## Notes

- The plugin uses PyCharm's `TypeEvalContext` to infer types.
- This is a lightweight viewer, not a full static analyzer.
- It only activates when the caret is on an actual word (identifier or literal).

## Requirements

- PyCharm 2024.3+ (Community or Professional) (may work on lower versions idk)

## Build Instructions

This plugin uses classes from the PyCharm Python plugin. Some of those classes I couldn't in public repositories, you need to **manually copy them from your installed IDE**.

### 1. Locate the required `.jar` files

If you are using **PyCharm 2024.3+**, find these files in:

```
PyCharm 2024.3.x\plugins\python-ce\lib\
```

You need:

- `python-ce.jar`
- `python-common.jar`

### 2. Copy the `.jar` files

Copy them to a `libs/` folder inside your project root (create it if needed):

```
<project-root>/libs/
├── python-ce.jar
└── python-common.jar
```

### 3. Build the plugin

```bash
./gradlew buildPlugin
```

plugin will be in `build/distributions`

## 📄 License

MIT
