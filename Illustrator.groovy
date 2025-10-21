import groovy.json.JsonOutput
import groovy.json.JsonSlurper

enum ImageType {
    PORTRAIT,
    LANDSCAPE,
    SQUARE
}

class Illustrator {
    String title
    ImgProvider provider
    String checkpoint
    ImageType style
    String input
    int seed

    Illustrator() {
        this.title = title
        this.provider = new ImgProvider()
        this.checkpoint = "dreamshaper_8.safetensors"
        this.style = ImageType.PORTRAIT
        this.input = input
        this.seed = new Random().nextInt(100000000) // 8 digits of random
    }

    String getPrompt(String input) {
        File jsonTemplate = new File("comfyui.json")
        def json = new JsonSlurper().parse(jsonTemplate)

        String height
        String width
        if (style == ImageType.PORTRAIT) {
            height = 768
            width = 512
        } else if (style == ImageType.LANDSCAPE) {
            height = 512
            width = 768
        } else {
            height = 512
            width = 512
        }

        json.prompt["3"].inputs.seed = seed
        json.prompt["4"].inputs.ckpt_name = checkpoint
        json.prompt["5"].inputs.width = width
        json.prompt["5"].inputs.height = height
        json.prompt["6"].inputs.text = input
        json.prompt["9"].inputs.filename_prefix = title
        def output = new JsonOutput().toJson(json)
        return output
    }

    String generateImage(String prompt) {
        URL url = new URL(provider.apiUrl)

        def post = url.openConnection()
        def body = prompt

        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write(body.getBytes("UTF-8"))

        def responseCode = post.getResponseCode()

        if (responseCode == 200) {
            return post.getInputStream().getText()
        }
        println post.getInputStream().getText()
        throw new RuntimeException("Something Went wrong.")
        return prompt
    }
}

class ImgProvider {
    File envFile = new File("Secrets/.env")
    String apiUrl
    String apiKey
    String token

    ImgProvider() {
        def envVars = [:]
        if (envFile.exists()) {
            envVars.putAll(loadDotEnv(envFile))
        }

        this.apiUrl = envVars.COMFYUI_API_URL
        this.apiKey = envVars.COMFYUI_API_KEY
        this.token = apiKey
    }

    public loadDotEnv(File path) {
        assert path.exists()
        Properties props = new Properties()
        path.withInputStream { props.load(it) }
        return props
    }
}
