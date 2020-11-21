using Newtonsoft.Json;

namespace FERApi.Models
{
    public class FERItem
    {
        [JsonProperty(PropertyName = "id")]
        public string Id { get; set; }
        [JsonProperty(PropertyName = "image")]
        public byte[] Image { get; set; }
        [JsonProperty(PropertyName = "emotionId")]
        public long EmotionID { get; set; }
    }
}