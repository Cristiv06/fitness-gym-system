import bcrypt

hashes = {
    "admin": b"$2b$10$M7X9k2F9tVWVBODyM7JuI.VpMRvmoenkXkqBKkvxbhXwMZb7d.91S",
    "user": b"$2b$10$W6D4V81VWXxeZjCKFXZGu.ZBbT9jnXYviYJeOWdJ0yFsHiq4zV3hG",
}
for name, h in hashes.items():
    print(name, "len", len(h))

for pwd in [b"Admin123!", b"User123!", b"admin", b"user"]:
    for label, h in hashes.items():
        try:
            ok = bcrypt.checkpw(pwd, h)
            if ok:
                print("MATCH", pwd, "->", label)
        except ValueError as e:
            print("ERR", pwd, label, e)
