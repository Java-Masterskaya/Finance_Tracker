# БЫСТРЫЙ СТАРТ (Gradle)

---

### 1️⃣ ЛОКАЛЬНАЯ РАЗРАБОТКА (H2 БД в памяти)

```bash
# Запуск с одной командой
./gradlew bootRun --args='--spring.profiles.active=local'

# Или в IDE (IntelliJ IDEA):
# Run → Edit Configurations → + → Gradle
#   Tasks: bootRun
#   Arguments: --args='--spring.profiles.active=local'
# Run → Shift+F10