import re

file_path = "app/src/main/java/com/example/data/model/AppStrings.kt"
with open(file_path, "r") as f:
    content = f.read()

# I will find TR, EN, AR, DE, FR definitions and make sure they only have exactly the correct keys.
# Wait, it's easier to just use vim/sed to delete the duplicates.
# Let's see the error lines.
