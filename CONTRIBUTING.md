# Contributing to SomoLink

Thank you for your interest in contributing to SomoLink! This document provides guidelines for contributing to the project.

## 📜 Code of Conduct

We are committed to providing a welcoming and inclusive environment. All contributors are expected to:

- Be respectful and inclusive
- Exercise empathy and kindness
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show courtesy and respect to other community members

## 🚀 Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally
3. **Set up the development environment** (see [Developer Guide](docs/guides/developer-guide.md))
4. **Create a branch** for your changes
5. **Make your changes** with clear commits
6. **Push to your fork** and submit a pull request

## 🔀 Branching Strategy

We use Git Flow:

- `main` - Production-ready code
- `develop` - Integration branch for features
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Emergency production fixes
- `release/*` - Release preparation

### Branch Naming Convention

```
feature/short-description
bugfix/issue-number-description
hotfix/critical-fix-description
```

Examples:
```
feature/solar-forecasting-v2
bugfix/123-fix-battery-reading
hotfix/api-gateway-memory-leak
```

## 💬 Commit Messages

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only changes
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or modifying tests
- `build`: Build system or external dependencies
- `ci`: CI/CD changes
- `chore`: Other changes that don't modify src or test files
- `revert`: Reverts a previous commit

### Scopes

- `edge-agent`
- `ai-platform`
- `api-gateway`
- `school-dashboard`
- `admin-dashboard`
- `billing`
- `data-ingestion`
- `infra`
- `docs`

### Examples

```bash
feat(ai-platform): add LSTM model for solar forecasting

Implements a new LSTM-based model for predicting solar power generation
based on historical data and weather forecasts.

- Added model architecture in models/solar.py
- Integrated with MLflow for experiment tracking
- Added unit tests for model inference

Closes #42

fix(edge-agent): resolve memory leak in telemetry collector

The telemetry collector was not properly releasing memory after each
collection cycle, causing gradual memory consumption.

- Fixed goroutine cleanup in collector.go
- Added proper context cancellation
- Increased test coverage to 85%

Fixes #156

docs(architecture): update deployment topology diagram

Updated the architecture overview to reflect the new regional deployment
model with multi-zone Kubernetes clusters.
```

## 🔍 Pull Request Process

1. **Update documentation** if you're changing functionality
2. **Add tests** for new features
3. **Ensure all tests pass** (`pnpm test`)
4. **Update CHANGELOG.md** if applicable
5. **Request review** from at least one maintainer
6. **Address review comments** promptly
7. **Squash commits** if requested

### Pull Request Template

When creating a PR, include:

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-reviewed my code
- [ ] Commented hard-to-understand areas
- [ ] Updated documentation
- [ ] No new warnings generated
- [ ] Tests pass locally

## Related Issues
Closes #issue_number
```

## 🧪 Testing Requirements

### Unit Tests

All new features must include unit tests:

- **Go**: `go test ./...`
- **Python**: `pytest`
- **TypeScript**: `jest`

### Integration Tests

For features involving multiple services, add integration tests.

### Test Coverage

Aim for minimum 80% code coverage for new code.

## 📝 Documentation

### Code Comments

- Write clear, concise comments
- Explain **why**, not **what**
- Use JSDoc/GoDoc/docstrings for public APIs

### Documentation Updates

Update relevant documentation in `/docs`:

- API changes → `/docs/api`
- Architecture changes → `/docs/architecture`
- Deployment changes → `/docs/deployment`
- New features → `/docs/guides`

## 🎨 Code Style

### TypeScript/JavaScript

- Use Prettier for formatting
- Follow Airbnb style guide
- Use ESLint rules in `.eslintrc`

```bash
pnpm format
pnpm lint
```

### Python

- Use Black for formatting
- Follow PEP 8
- Type hints for all functions

```bash
black .
flake8 .
mypy .
```

### Go

- Use `gofmt` for formatting
- Follow Go conventions
- Use `golangci-lint`

```bash
gofmt -w .
go vet ./...
golangci-lint run
```

## 🐛 Reporting Bugs

### Before Submitting

1. Check existing issues
2. Verify bug in latest version
3. Collect error logs and steps to reproduce

### Bug Report Template

```markdown
**Describe the bug**
Clear description of the issue

**To Reproduce**
1. Step 1
2. Step 2
3. See error

**Expected behavior**
What you expected to happen

**Screenshots**
If applicable

**Environment:**
- OS: [e.g., Ubuntu 22.04]
- Version: [e.g., 1.2.3]
- Browser: [if applicable]

**Additional context**
Any other relevant information
```

## 💡 Feature Requests

### Before Submitting

1. Check if feature already exists
2. Search existing feature requests
3. Consider if it fits project scope

### Feature Request Template

```markdown
**Is your feature request related to a problem?**
Clear description of the problem

**Describe the solution you'd like**
Clear description of what you want to happen

**Describe alternatives you've considered**
Other solutions you've thought about

**Additional context**
Mockups, examples, references
```

## 🔐 Security Issues

**DO NOT** create public issues for security vulnerabilities.

Instead, email: security@wekkitech.co.ke

Include:
- Description of vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

## 📊 Performance Considerations

When contributing performance-critical code:

- Profile before and after changes
- Include benchmark results
- Consider memory usage
- Test with production-like data volumes

## 🌍 Internationalization

When adding user-facing strings:

- Use i18n functions
- Add translations for: English, Swahili
- Consider RTL languages for future

## ♿ Accessibility

For frontend contributions:

- Follow WCAG 2.1 AA standards
- Test with screen readers
- Ensure keyboard navigation
- Use semantic HTML
- Provide alt text for images

## 📦 Dependencies

### Adding Dependencies

- Justify the need
- Check license compatibility
- Consider bundle size (frontend)
- Verify maintenance status
- Document in PR description

### Updating Dependencies

- Test thoroughly
- Update documentation
- Check breaking changes
- Update lock files

## 🚢 Release Process

Maintainers handle releases:

1. Version bump via Changesets
2. Update CHANGELOG.md
3. Create release branch
4. Run full test suite
5. Deploy to staging
6. QA testing
7. Deploy to production
8. Tag release
9. Publish release notes

## 🏆 Recognition

Contributors are recognized in:

- CONTRIBUTORS.md file
- Release notes
- Project website

## 📞 Getting Help

- **Slack**: #somolink-dev
- **Email**: dev@wekkitech.co.ke
- **Discussions**: GitHub Discussions
- **Office Hours**: Fridays 2-4 PM EAT

## 📚 Resources

- [Developer Guide](docs/guides/developer-guide.md)
- [Architecture Overview](docs/architecture/overview.md)
- [API Documentation](docs/api/README.md)
- [Code Examples](examples/)

## ✅ Checklist for First-Time Contributors

- [ ] Read Code of Conduct
- [ ] Set up development environment
- [ ] Join Slack workspace
- [ ] Find a "good first issue"
- [ ] Ask questions if stuck
- [ ] Submit your first PR!

---

Thank you for contributing to SomoLink! Together, we're bringing connectivity and education to underserved communities. 🌍📚⚡
