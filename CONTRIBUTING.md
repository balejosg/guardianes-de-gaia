# 🤝 Contributing to Guardianes de Gaia

Thank you for your interest in contributing to Guardianes de Gaia!

## 📋 Development Guidelines

### 1. Test-Driven Development (TDD)

All code changes must follow TDD:

1. Write a failing test first
2. Write minimal code to make it pass
3. Refactor while keeping tests green
4. Run `make test-all` before committing

### 2. Vertical Slicing

Follow the [Vertical Slicing Strategy](docs/VERTICAL_SLICING_STRATEGY.md):
- Each feature spans Mobile → Backend → Database
- Complete one slice before starting another

### 3. Domain-Driven Design

Use the [Ubiquitous Language](docs/UBIQUITOUS_LANGUAGE.md) for naming:
- `Guardian`, `Pacto`, `Energía Vital`, `Ruta Mágica`, etc.

## 🔄 Workflow

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/your-feature`
3. **Write tests** first
4. **Implement** the feature
5. **Run** all tests: `make test-all`
6. **Commit** with semantic messages (see below)
7. **Push** to your fork
8. **Open** a Pull Request

## ✍️ Commit Messages

Use semantic commit messages:

| Prefix | Purpose |
|--------|---------|
| `feat:` | New functionality |
| `fix:` | Bug fixes |
| `test:` | Adding tests |
| `docs:` | Documentation |
| `refactor:` | Code refactoring |
| `chore:` | Maintenance tasks |

**Example**: `feat: add QR code scanning for card collection`

## ✅ Pre-commit Checklist

- [ ] All tests pass: `make test-all`
- [ ] Code is formatted: `make format`
- [ ] Linting passes: `make lint`
- [ ] No disabled or skipped tests
- [ ] Documentation updated if needed

## 📚 Resources

- [Project Overview](docs/PROYECTO.md)
- [Tech Stack](docs/TECH_STACK.md)
- [LLM Guidelines](CLAUDE.md)
