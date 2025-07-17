{{/*
Expand the name of the chart.
*/}}
{{- define "guardianes-de-gaia.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "guardianes-de-gaia.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "guardianes-de-gaia.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "guardianes-de-gaia.labels" -}}
helm.sh/chart: {{ include "guardianes-de-gaia.chart" . }}
{{ include "guardianes-de-gaia.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "guardianes-de-gaia.selectorLabels" -}}
app.kubernetes.io/name: {{ include "guardianes-de-gaia.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "guardianes-de-gaia.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "guardianes-de-gaia.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create the name of the secret
*/}}
{{- define "guardianes-de-gaia.secretName" -}}
{{- if .Values.existingSecret }}
{{- .Values.existingSecret }}
{{- else }}
{{- printf "%s-secret" (include "guardianes-de-gaia.fullname" .) }}
{{- end }}
{{- end }}

{{/*
MySQL fullname
*/}}
{{- define "guardianes-de-gaia.mysql.fullname" -}}
{{- if .Values.mysql.enabled }}
{{- printf "%s-mysql" .Release.Name }}
{{- else }}
{{- .Values.externalDatabase.host }}
{{- end }}
{{- end }}

{{/*
Redis fullname
*/}}
{{- define "guardianes-de-gaia.redis.fullname" -}}
{{- if .Values.redis.enabled }}
{{- printf "%s-redis" .Release.Name }}
{{- else }}
{{- .Values.externalRedis.host }}
{{- end }}
{{- end }}

{{/*
RabbitMQ fullname
*/}}
{{- define "guardianes-de-gaia.rabbitmq.fullname" -}}
{{- if .Values.rabbitmq.enabled }}
{{- printf "%s-rabbitmq" .Release.Name }}
{{- else }}
{{- .Values.externalRabbitmq.host }}
{{- end }}
{{- end }}

{{/*
Return the proper image name
*/}}
{{- define "guardianes-de-gaia.image" -}}
{{- $registryName := .Values.backend.image.registry -}}
{{- $repositoryName := .Values.backend.image.repository -}}
{{- $tag := .Values.backend.image.tag | toString -}}
{{- if .Values.global.imageRegistry }}
    {{- printf "%s/%s:%s" .Values.global.imageRegistry $repositoryName $tag -}}
{{- else -}}
    {{- printf "%s/%s:%s" $registryName $repositoryName $tag -}}
{{- end -}}
{{- end }}

{{/*
Return the proper Docker Image Registry Secret Names
*/}}
{{- define "guardianes-de-gaia.imagePullSecrets" -}}
{{- include "common.images.pullSecrets" (dict "images" (list .Values.backend.image) "global" .Values.global) -}}
{{- end }}

{{/*
Validate values
*/}}
{{- define "guardianes-de-gaia.validateValues" -}}
{{- if and (not .Values.mysql.enabled) (not .Values.externalDatabase.host) }}
guardianes-de-gaia: database
    You must enable MySQL or provide an external database host.
{{- end }}
{{- if and (not .Values.redis.enabled) (not .Values.externalRedis.host) }}
guardianes-de-gaia: redis
    You must enable Redis or provide an external Redis host.
{{- end }}
{{- if and (not .Values.rabbitmq.enabled) (not .Values.externalRabbitmq.host) }}
guardianes-de-gaia: rabbitmq
    You must enable RabbitMQ or provide an external RabbitMQ host.
{{- end }}
{{- end }}

{{/*
Create environment-specific resource names
*/}}
{{- define "guardianes-de-gaia.resourceName" -}}
{{- $name := include "guardianes-de-gaia.fullname" . -}}
{{- if .Values.global.environment }}
{{- printf "%s-%s" $name .Values.global.environment }}
{{- else }}
{{- $name }}
{{- end }}
{{- end }}

{{/*
Generate certificates
*/}}
{{- define "guardianes-de-gaia.gen-certs" -}}
{{- $ca := genCA "guardianes-de-gaia-ca" 365 -}}
{{- $cert := genSignedCert (include "guardianes-de-gaia.fullname" .) nil (list (printf "%s.%s" (include "guardianes-de-gaia.fullname" .) .Release.Namespace) (printf "%s.%s.svc" (include "guardianes-de-gaia.fullname" .) .Release.Namespace)) 365 $ca -}}
ca.crt: {{ $ca.Cert | b64enc }}
tls.crt: {{ $cert.Cert | b64enc }}
tls.key: {{ $cert.Key | b64enc }}
{{- end }}

{{/*
Return the target Kubernetes version
*/}}
{{- define "guardianes-de-gaia.kubeVersion" -}}
{{- if .Values.global.kubeVersion }}
{{- .Values.global.kubeVersion -}}
{{- else }}
{{- .Capabilities.KubeVersion.Version -}}
{{- end }}
{{- end }}

{{/*
Compile all warnings into a single message, and call fail.
*/}}
{{- define "guardianes-de-gaia.validateValues.fail" -}}
{{- $messages := list -}}
{{- $messages := append $messages (include "guardianes-de-gaia.validateValues" .) -}}
{{- $messages := without $messages "" -}}
{{- $message := join "\n" $messages -}}
{{- if $message -}}
{{- printf "\nVALUES VALIDATION:\n%s" $message | fail -}}
{{- end -}}
{{- end }}