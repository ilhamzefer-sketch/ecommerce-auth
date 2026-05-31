# Notebook Developer Runbook

Bu notebook developer masinidir. Burada kod yazilir, test edilir ve GitHub-a push edilir. Image build ve deploy update isi GitHub Actions terefinden gorulur.

## Bir defe hazirliq

Notebookda Java 21 hazir olmalidir:

```powershell
java -version
$env:JAVA_HOME
```

Eger `java` tapilmirsa, JDK 21 install et ve `JAVA_HOME`/`PATH` duzelt. Bu olmadan `.\gradlew.bat test` islemeyecek.

1. GitHub-da bu repo oldugunu yoxla:

```text
https://github.com/ilhamzefer-sketch/ecommerce-auth
```

2. GitHub-da GitOps repo oldugunu yoxla:

```text
https://github.com/ilhamzefer-sketch/ecommerce-gitops
```

3. `ecommerce-auth` reposunda secret yarat:

```text
Settings > Secrets and variables > Actions > New repository secret
Name: GITOPS_TOKEN
Value: GitHub token with write access to ilhamzefer-sketch/ecommerce-gitops
```

Token ucun minimum icazeler:

```text
repo
write:packages
read:packages
```

`GITOPS_TOKEN` lazimdir ki, `ecommerce-auth` workflow-u image build edenden sonra `ecommerce-gitops` reposundaki Kubernetes manifestini yeni image tag ile update ede bilsin.

## Normal developer flow

1. Auth servisinde deyisiklik et:

```powershell
cd C:\Users\Dell\Desktop\ecommerce-auth
```

2. Lokal test et:

```powershell
.\gradlew.bat test
```

3. Deyisiklikleri commit ve push et:

```powershell
git status
git add .
git commit -m "Update ecommerce auth"
git push origin main
```

4. GitHub Actions-i izleme yeri:

```text
https://github.com/ilhamzefer-sketch/ecommerce-auth/actions
```

Workflow ugurlu olanda bunlar bas verir:

```text
1. Docker image build olunur
2. Image GHCR-a push olunur:
   ghcr.io/ilhamzefer-sketch/ecommerce-auth:<commit-sha>
3. ecommerce-gitops/apps/ecommerce-auth/app.yaml yeni image tag ile update olunur
4. GitOps repo-ya avtomatik commit push olunur
```

5. GitOps update commitini yoxla:

```powershell
cd C:\Users\Dell\Desktop\ecommerce-gitops
git pull origin main
git log --oneline -5
git diff HEAD~1 HEAD -- apps/ecommerce-auth/app.yaml
```

## Neyi notebookda etmeli deyilsen

Notebookda Argo CD-ni daimi run etmek mecburi deyil. Argo CD server PC-de islemelidir. Notebookun esas isi kod deyisib push etmekdir.

Notebookda image-i elle build/push etmek de normal flow deyil. Bunu GitHub Actions edir.

## Problem olanda yoxla

GitHub Actions fail olarsa:

```text
1. ecommerce-auth/actions sehifesindeki failed job loguna bax
2. GITOPS_TOKEN secret-i movcuddurmu yoxla
3. Tokenin ecommerce-gitops repo-ya write access-i oldugunu yoxla
4. GHCR package private qalibsa, server PC-de read:packages token istifade olundugunu yoxla
```

Server PC-de app update olmursa:

```text
1. ecommerce-gitops repo-da app.yaml image tag deyisibmi yoxla
2. Argo CD application sync statusuna bax
3. Server PC-de ghcr-pull-secret duz token ile yaradilibmi yoxla
```
