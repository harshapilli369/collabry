/**
 * Shared option lists used by both the brand industry dropdown and the
 * influencer niche dropdown, ensuring the two fields stay in sync.
 */
export const INDUSTRY_NICHE_OPTIONS = [
    {
        label: 'Fashion & Beauty',
        options: [
            { label: 'Apparel & Clothing',          value: 'Apparel & Clothing' },
            { label: 'Beauty & Cosmetics',           value: 'Beauty & Cosmetics' },
            { label: 'Luxury & Accessories',         value: 'Luxury & Accessories' },
            { label: 'Jewellery & Watches',          value: 'Jewellery & Watches' },
            { label: 'Footwear',                     value: 'Footwear' },
        ],
    },
    {
        label: 'Technology',
        options: [
            { label: 'Consumer Electronics',         value: 'Consumer Electronics' },
            { label: 'Software & SaaS',              value: 'Software & SaaS' },
            { label: 'Gaming & Esports',             value: 'Gaming & Esports' },
            { label: 'Smart Home & IoT',             value: 'Smart Home & IoT' },
            { label: 'Mobile & Apps',                value: 'Mobile & Apps' },
        ],
    },
    {
        label: 'Food & Beverage',
        options: [
            { label: 'Food & Snacks',                value: 'Food & Snacks' },
            { label: 'Beverages & Drinks',           value: 'Beverages & Drinks' },
            { label: 'Health Food & Supplements',    value: 'Health Food & Supplements' },
            { label: 'Restaurants & Dining',         value: 'Restaurants & Dining' },
        ],
    },
    {
        label: 'Health & Wellness',
        options: [
            { label: 'Fitness & Sports',             value: 'Fitness & Sports' },
            { label: 'Mental Health & Wellness',     value: 'Mental Health & Wellness' },
            { label: 'Nutrition & Diet',             value: 'Nutrition & Diet' },
            { label: 'Personal Care',                value: 'Personal Care' },
        ],
    },
    {
        label: 'Lifestyle',
        options: [
            { label: 'Travel & Hospitality',         value: 'Travel & Hospitality' },
            { label: 'Home & Interior',              value: 'Home & Interior' },
            { label: 'Pet Care',                     value: 'Pet Care' },
            { label: 'Outdoor & Adventure',          value: 'Outdoor & Adventure' },
            { label: 'Automotive',                   value: 'Automotive' },
        ],
    },
    {
        label: 'Media & Entertainment',
        options: [
            { label: 'Music & Audio',                value: 'Music & Audio' },
            { label: 'Film & TV',                    value: 'Film & TV' },
            { label: 'Books & Publishing',           value: 'Books & Publishing' },
            { label: 'Events & Live Entertainment',  value: 'Events & Live Entertainment' },
        ],
    },
    {
        label: 'Finance & Business',
        options: [
            { label: 'Fintech & Payments',           value: 'Fintech & Payments' },
            { label: 'Insurance',                    value: 'Insurance' },
            { label: 'E-commerce & Retail',          value: 'E-commerce & Retail' },
            { label: 'Real Estate',                  value: 'Real Estate' },
        ],
    },
    {
        label: 'Education',
        options: [
            { label: 'Online Learning',              value: 'Online Learning' },
            { label: 'Kids & Parenting',             value: 'Kids & Parenting' },
            { label: 'Professional Development',     value: 'Professional Development' },
        ],
    },
]

/**
 * City, Country location options grouped by region.
 */
export const LOCATION_OPTIONS = [
    {
        label: 'North America',
        options: [
            { label: 'New York, USA',         value: 'New York, USA' },
            { label: 'Los Angeles, USA',      value: 'Los Angeles, USA' },
            { label: 'Chicago, USA',          value: 'Chicago, USA' },
            { label: 'Houston, USA',          value: 'Houston, USA' },
            { label: 'Miami, USA',            value: 'Miami, USA' },
            { label: 'San Francisco, USA',    value: 'San Francisco, USA' },
            { label: 'Seattle, USA',          value: 'Seattle, USA' },
            { label: 'Austin, USA',           value: 'Austin, USA' },
            { label: 'Boston, USA',           value: 'Boston, USA' },
            { label: 'Denver, USA',           value: 'Denver, USA' },
            { label: 'Atlanta, USA',          value: 'Atlanta, USA' },
            { label: 'Toronto, Canada',       value: 'Toronto, Canada' },
            { label: 'Vancouver, Canada',     value: 'Vancouver, Canada' },
            { label: 'Montreal, Canada',      value: 'Montreal, Canada' },
            { label: 'Calgary, Canada',       value: 'Calgary, Canada' },
            { label: 'Halifax, Canada',       value: 'Halifax, Canada' },
        ],
    },
    {
        label: 'Europe',
        options: [
            { label: 'London, UK',            value: 'London, UK' },
            { label: 'Manchester, UK',        value: 'Manchester, UK' },
            { label: 'Paris, France',         value: 'Paris, France' },
            { label: 'Berlin, Germany',       value: 'Berlin, Germany' },
            { label: 'Munich, Germany',       value: 'Munich, Germany' },
            { label: 'Amsterdam, Netherlands', value: 'Amsterdam, Netherlands' },
            { label: 'Madrid, Spain',         value: 'Madrid, Spain' },
            { label: 'Barcelona, Spain',      value: 'Barcelona, Spain' },
            { label: 'Rome, Italy',           value: 'Rome, Italy' },
            { label: 'Milan, Italy',          value: 'Milan, Italy' },
            { label: 'Stockholm, Sweden',     value: 'Stockholm, Sweden' },
            { label: 'Copenhagen, Denmark',   value: 'Copenhagen, Denmark' },
            { label: 'Zurich, Switzerland',   value: 'Zurich, Switzerland' },
            { label: 'Dublin, Ireland',       value: 'Dublin, Ireland' },
            { label: 'Lisbon, Portugal',      value: 'Lisbon, Portugal' },
        ],
    },
    {
        label: 'Asia Pacific',
        options: [
            { label: 'Tokyo, Japan',          value: 'Tokyo, Japan' },
            { label: 'Seoul, South Korea',    value: 'Seoul, South Korea' },
            { label: 'Singapore, Singapore',  value: 'Singapore, Singapore' },
            { label: 'Sydney, Australia',     value: 'Sydney, Australia' },
            { label: 'Melbourne, Australia',  value: 'Melbourne, Australia' },
            { label: 'Mumbai, India',         value: 'Mumbai, India' },
            { label: 'Delhi, India',          value: 'Delhi, India' },
            { label: 'Bangalore, India',      value: 'Bangalore, India' },
            { label: 'Shanghai, China',       value: 'Shanghai, China' },
            { label: 'Hong Kong, China',      value: 'Hong Kong, China' },
            { label: 'Bangkok, Thailand',     value: 'Bangkok, Thailand' },
            { label: 'Kuala Lumpur, Malaysia', value: 'Kuala Lumpur, Malaysia' },
            { label: 'Jakarta, Indonesia',    value: 'Jakarta, Indonesia' },
        ],
    },
    {
        label: 'Middle East & Africa',
        options: [
            { label: 'Dubai, UAE',            value: 'Dubai, UAE' },
            { label: 'Abu Dhabi, UAE',        value: 'Abu Dhabi, UAE' },
            { label: 'Riyadh, Saudi Arabia',  value: 'Riyadh, Saudi Arabia' },
            { label: 'Cairo, Egypt',          value: 'Cairo, Egypt' },
            { label: 'Lagos, Nigeria',        value: 'Lagos, Nigeria' },
            { label: 'Nairobi, Kenya',        value: 'Nairobi, Kenya' },
            { label: 'Cape Town, South Africa', value: 'Cape Town, South Africa' },
            { label: 'Johannesburg, South Africa', value: 'Johannesburg, South Africa' },
        ],
    },
    {
        label: 'Latin America',
        options: [
            { label: 'São Paulo, Brazil',     value: 'São Paulo, Brazil' },
            { label: 'Rio de Janeiro, Brazil', value: 'Rio de Janeiro, Brazil' },
            { label: 'Buenos Aires, Argentina', value: 'Buenos Aires, Argentina' },
            { label: 'Mexico City, Mexico',   value: 'Mexico City, Mexico' },
            { label: 'Bogotá, Colombia',      value: 'Bogotá, Colombia' },
            { label: 'Santiago, Chile',       value: 'Santiago, Chile' },
            { label: 'Lima, Peru',            value: 'Lima, Peru' },
        ],
    },
]
