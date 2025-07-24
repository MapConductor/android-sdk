import os
import re

MODULES = [
    'mapconductor-core',
    'mapconductor-for-googlemaps',
    'mapconductor-for-mapbox',
    'mapconductor-for-here',
    'mapconductor-for-arcgis',
    'mapconductor-icons',
    'example-app',
]

CLASS_RE = re.compile(
    r"^(?:public\s+|internal\s+|protected\s+|private\s+)?"
    r"(?:open\s+|abstract\s+|data\s+|sealed\s+)?class\s+"
    r"(\w+)\s*(?:\([^)]*\))?\s*(?::\s*([^\s{]+))?"
)
INTERFACE_RE = re.compile(
    r"^(?:public\s+|internal\s+|protected\s+|private\s+)?"
    r"(?:sealed\s+)?interface\s+"
    r"(\w+)\s*(?:\([^)]*\))?\s*(?::\s*([^\s{]+))?"
)


def parse_kotlin_file(path):
    classes = []
    with open(path, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            m = CLASS_RE.match(line)
            if m:
                name, parent = m.groups()
                if name and name != 'class':
                    parents = [p.strip() for p in parent.split(',')] if parent else []
                    classes.append((name, parents))
                continue
            m = INTERFACE_RE.match(line)
            if m:
                name, parent = m.groups()
                if name and name != 'class':
                    parents = [p.strip() for p in parent.split(',')] if parent else []
                    classes.append((name, parents))
    return classes


def collect_module_classes(module_path):
    results = []
    src_path = os.path.join(module_path, 'src')
    if not os.path.isdir(src_path):
        return results
    for root, _, files in os.walk(src_path):
        for fname in files:
            if fname.endswith('.kt'):
                filepath = os.path.join(root, fname)
                results.extend(parse_kotlin_file(filepath))
    return results


def generate_module_diagram(module, classes):
    lines = ["```mermaid", "classDiagram"]
    # declare all classes
    declared = set()
    for cls, parents in classes:
        if not cls or cls == 'class':
            continue
        lines.append(f"    class {cls}")
        declared.add(cls)
    # relationships
    for cls, parents in classes:
        for parent in parents:
            parent = parent.split('<')[0]
            if not parent or parent == 'class':
                continue
            if parent not in declared:
                lines.append(f"    class {parent}")
                declared.add(parent)
            lines.append(f"    {parent} <|-- {cls}")
    lines.append("```")
    return "\n".join(lines)


if __name__ == '__main__':
    diagrams = {}
    for module in MODULES:
        classes = collect_module_classes(module)
        if classes:
            diagrams[module] = generate_module_diagram(module, classes)

    output_lines = ["# Class Diagrams\n"]
    for module in MODULES:
        if module not in diagrams:
            continue
        output_lines.append(f"## {module}\n")
        output_lines.append(diagrams[module])
        output_lines.append("")
    with open('docs/ClassDiagrams.md', 'w', encoding='utf-8') as f:
        f.write("\n".join(output_lines))
    print('Generated docs/ClassDiagrams.md')
