package com.example.domain

data class CityLocation(
    val nameBn: String,
    val nameEn: String,
    val latitude: Double,
    val longitude: Double
) {
    fun displayName(isBangla: Boolean): String = if (isBangla) nameBn else nameEn
}

object CityCatalog {
    val cities: List<CityLocation> = listOf(
        CityLocation("ঢাকা, বাংলাদেশ", "Dhaka, Bangladesh", 23.8103, 90.4125),
        CityLocation("চট্টগ্রাম, বাংলাদেশ", "Chattogram, Bangladesh", 22.3569, 91.7832),
        CityLocation("সিলেট, বাংলাদেশ", "Sylhet, Bangladesh", 24.8949, 91.8687),
        CityLocation("রাজশাহী, বাংলাদেশ", "Rajshahi, Bangladesh", 24.3745, 88.6042),
        CityLocation("খুলনা, বাংলাদেশ", "Khulna, Bangladesh", 22.8456, 89.5403),
        CityLocation("বরিশাল, বাংলাদেশ", "Barishal, Bangladesh", 22.7010, 90.3535),
        CityLocation("রংপুর, বাংলাদেশ", "Rangpur, Bangladesh", 25.7439, 89.2752),
        CityLocation("ময়মনসিংহ, বাংলাদেশ", "Mymensingh, Bangladesh", 24.7471, 90.4203),
        CityLocation("কুমিল্লা, বাংলাদেশ", "Cumilla, Bangladesh", 23.4607, 91.1809),
        CityLocation("নারায়ণগঞ্জ, বাংলাদেশ", "Narayanganj, Bangladesh", 23.6238, 90.5000),
        CityLocation("গাজীপুর, বাংলাদেশ", "Gazipur, Bangladesh", 23.9999, 90.4203),
        CityLocation("বগুড়া, বাংলাদেশ", "Bogura, Bangladesh", 24.8465, 89.3773),
        CityLocation("কক্সবাজার, বাংলাদেশ", "Cox's Bazar, Bangladesh", 21.4272, 92.0058),
        CityLocation("যশোর, বাংলাদেশ", "Jashore, Bangladesh", 23.1634, 89.2182),
        CityLocation("দিনাজপুর, বাংলাদেশ", "Dinajpur, Bangladesh", 25.6279, 88.6332),
        CityLocation("টাঙ্গাইল, বাংলাদেশ", "Tangail, Bangladesh", 24.2513, 89.9167),
        CityLocation("ফেনী, বাংলাদেশ", "Feni, Bangladesh", 23.0159, 91.3976),
        CityLocation("নোয়াখালী, বাংলাদেশ", "Noakhali, Bangladesh", 22.8696, 91.0994),
        CityLocation("মক্কা, সৌদি আরব", "Makkah, Saudi Arabia", 21.4225, 39.8262),
        CityLocation("মদিনা, সৌদি আরব", "Madinah, Saudi Arabia", 24.4672, 39.6111),
        CityLocation("রিয়াদ, সৌদি আরব", "Riyadh, Saudi Arabia", 24.7136, 46.6753),
        CityLocation("দুবাই, সংযুক্ত আরব আমিরাত", "Dubai, UAE", 25.2048, 55.2708),
        CityLocation("দোহা, কাতার", "Doha, Qatar", 25.2854, 51.5310),
        CityLocation("কুয়ালালামপুর, মালয়েশিয়া", "Kuala Lumpur, Malaysia", 3.1390, 101.6869),
        CityLocation("সিঙ্গাপুর", "Singapore", 1.3521, 103.8198),
        CityLocation("কলকাতা, ভারত", "Kolkata, India", 22.5726, 88.3639),
        CityLocation("লন্ডন, যুক্তরাজ্য", "London, UK", 51.5072, -0.1276),
        CityLocation("নিউ ইয়র্ক, যুক্তরাষ্ট্র", "New York, USA", 40.7128, -74.0060)
    )
}
