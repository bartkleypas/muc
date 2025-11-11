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
    int steps

    Illustrator() {
        this.title = title
        this.provider = new ImgProvider()
        this.checkpoint = "sdxl/sd_xl_base_1.0.safetensors"
        this.style = ImageType.PORTRAIT
        this.input = input
        this.seed = new Random().nextInt(100000000) // 8 digits of random
        this.steps = 25
    }

    // So, lets assume we are given a string that contains <IMAGE_DESC> tags
    // Why? Because we tell the LLM in its system prompt to format it like this
    // Lets hope we have an 'if output.contains("<IMAGE_DESC>") {}' control?
    String promptToJson(String input) {
        assert input.contains("<IMAGE_DESC>")

        def matcher = input =~ /<IMAGE_DESC>(.*?)<\/IMAGE_DESC>/
        def imgDesc = matcher[0][1].trim()
        def out = getComfyUiJson(imgDesc)
        return out
    }

    String getComfyUiJson(String input) {
        File jsonTemplate = new File("comfyui.json")
        def json = new JsonSlurper().parse(jsonTemplate)

        String height
        String width
        if (style == ImageType.PORTRAIT) {
            height = 1216
            width = 832
        } else if (style == ImageType.LANDSCAPE) {
            height = 832
            width = 1216
        } else {
            height = 1024
            width = 1024
        }

        json.prompt["4"].inputs.ckpt_name = checkpoint
        json.prompt["5"].inputs.width = width
        json.prompt["5"].inputs.height = height
        json.prompt["6"].inputs.text = input
        json.prompt["10"].inputs.noise_seed = seed
        json.prompt["10"].inputs.steps = steps
        json.prompt["15"].inputs.text = input
        json.prompt["19"].inputs.filename_prefix = title
        def output = new JsonOutput().toJson(json)
        return output
    }

    String generateImage(String comfyUiJson) {
        URL url = new URL(provider.apiUrl)

        def post = url.openConnection()
        def body = comfyUiJson

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
