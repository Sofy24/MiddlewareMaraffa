#!/bin/bash
# To test CI locally i used act tool https://github.com/nektos/act , thank you Nektos

# Also thanks to chat.openai.com to help me build scripts in a minute


# Percorso del file di configurazione
file="app/.env.example"

declare -a config_array

while IFS= read -r line; do
    line=$(echo "$line" | tr -d '[:space:]')
    config_array+=("$line")
done < "$file"

formatted_config=""
for config in "${config_array[@]}"; do
    formatted_config+=",$config"
done

# Rimuovi la virgola iniziale
formatted_config=${formatted_config:1}

echo $formatted_config
#act -s "$formatted_config"
act -s "$formatted_config"