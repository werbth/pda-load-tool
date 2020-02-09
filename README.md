# PDA Load Tool

Tool for loading data for Entando PDA Plugin

## Load Widgets

Sample request:
```text
POST http://localhost:8089/widgets/load
{
	"entandoApi": "http://app-entando-pda-test.51.91.30.184.nip.io/entando-de-app",
	"authToken": "<OAUTH_TOKEN_HERE>",
	"widgets": ["task-list", "summary-card", "task-comments", "process-form", "task-completion-form", "task-details"],
	"serviceUrl": "/pda",
	"bundleId": "pda",
	"resources": [
		"static/css/2.8c216021.chunk.css",
		"static/css/main.f5737de9.chunk.css",
		"static/js/2.cabe14f5.chunk.js", 
		"static/js/main.a650ca66.chunk.js",
		"static/js/runtime-main.483f67fa.js"]
}

```
