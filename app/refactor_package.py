import os
import shutil

def refactor():
    print("Starting package refactor to com.shelem.scoresheet...")
    
    # 1. Move directory structures
    sources = [
        "app/src/main/java/com/example",
        "app/src/test/java/com/example",
        "app/src/androidTest/java/com/example"
    ]
    
    for src in sources:
        if os.path.exists(src):
            base = src.split("com")[0] # app/src/main/java/
            new_dir = os.path.join(base, "com", "shelem", "scoresheet")
            os.makedirs(new_dir, exist_ok=True)
            
            for item in os.listdir(src):
                s_path = os.path.join(src, item)
                d_path = os.path.join(new_dir, item)
                if os.path.exists(d_path):
                    if os.path.isdir(d_path):
                        shutil.copytree(s_path, d_path, dirs_exist_ok=True)
                        shutil.rmtree(s_path)
                    else:
                        os.remove(d_path)
                        shutil.move(s_path, d_path)
                else:
                    shutil.move(s_path, d_path)
            shutil.rmtree(src, ignore_errors=True)
            print(f"Moved {src} to {new_dir}")

    # 2. Update file contents (replace com.example with com.shelem.scoresheet)
    for root, dirs, files in os.walk("app"):
        for file in files:
            if file.endswith((".kt", ".kts", ".xml")):
                path = os.path.join(root, file)
                with open(path, "r", encoding="utf-8") as f:
                    content = f.read()
                
                new_content = content.replace("com.example", "com.shelem.scoresheet")
                if new_content != content:
                    with open(path, "w", encoding="utf-8") as f:
                        f.write(new_content)
                    print(f"Updated references in {path}")

    print("Refactor complete!")

if __name__ == "__main__":
    refactor()
