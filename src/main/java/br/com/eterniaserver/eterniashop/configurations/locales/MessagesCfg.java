package br.com.eterniaserver.eterniashop.configurations.locales;

import br.com.eterniaserver.eternialib.core.baseobjects.CustomizableMessage;
import br.com.eterniaserver.eternialib.core.enums.ConfigurationCategory;
import br.com.eterniaserver.eternialib.core.interfaces.ReloadableConfiguration;
import br.com.eterniaserver.eterniashop.Constants;
import br.com.eterniaserver.eterniashop.enums.Messages;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MessagesCfg implements ReloadableConfiguration {

    final CustomizableMessage[] customizableMessages = new CustomizableMessage[Messages.values().length];

    final String[] messages;

    public MessagesCfg(final String[] messages) {
        this.messages = messages;

        addDefault(Messages.COMMAND_COST, "$7Esse comando irá custar $3{0} $7para confirmar use $6/confirmar $7ou $6/cancelar $7para cancelar$8.", "0: custo do commando");
        addDefault(Messages.TELEPORT_TIMING, "$7Você será teleportado em $3{0} $7segundos$8.", "0: tempo");
        addDefault(Messages.TELEPORT_MOVED, "$7Você se moveu e por isso o teleporte foi cancelado$8.", null);
        addDefault(Messages.RELOAD, "$7Reiniciando configurações$8.", null);
        addDefault(Messages.ETERNIASHOP, "$7EterniaShop$8, $7by $3yurinogueira$8.", null);
        addDefault(Messages.NOT_SAFE, "$7O local onde você está não é seguro para definir uma loja$8.", null);
        addDefault(Messages.SHOP_DEFINED, "$7Loja definida com sucesso$8.", null);
        addDefault(Messages.NOT_MONEY, "$7Você não possui dinheiro suficiênte$8, $7faltam $3{0}$8.", "0: quantia em money que falta");
        addDefault(Messages.SERVER_SHOP_NOT_DEFINED, "$7Loja do servidor não definida ainda$8.", null);
        addDefault(Messages.SHOP_WELCOME, "$7Você foi teleportado até a loja do servidor$8.", null);
        addDefault(Messages.SHOP_PLAYER_WELCOME, "$7Bem vindo a loja $3{0}$8.", "0: nome da loja");
        addDefault(Messages.SHOP_PLAYER_NOT, "$7Esse jogador não possui uma loja$8.", null);
        addDefault(Messages.ETERNIASHOP_LOADED, "$3EterniaShop $7carregado$8, $7foram carregadas $3{0}$7 lojas$8.", "0: quantidade de lojas");
        addDefault(Messages.SHOP_DELETED, "$7Você deletou sua loja$8.", null);
        addDefault(Messages.SHOP_DELETE_CONFIRM, "$7Você tem certeza que deseja deletar sua loja$8? $6/confirmar $7para confirmar ou $6/cancelar $7para cancelar$8.", null);
        addDefault(Messages.NOT_SHOP, "$7Você não possui uma loja$8.", null);
        addDefault(Messages.SERVER_SHOP_DEFINED, "$7Loja do servidor definida com sucesso$8.", null);
    }

    private void addDefault(final Messages id, final String text, final String notes) {
        customizableMessages[id.ordinal()] = new CustomizableMessage(text, notes);
    }

    @Override
    public ConfigurationCategory category() {
        return ConfigurationCategory.GENERIC;
    }

    @Override
    public void executeConfig() {
        final FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File(Constants.MESSAGES_FILE_PATH));

        for (final Messages entry : Messages.values()) {
            CustomizableMessage messageData = customizableMessages[entry.ordinal()];

            if (messageData == null) {
                messageData = new CustomizableMessage("Mensagem faltando para $3" + entry.name() + "$8.", null);
            }

            messages[entry.ordinal()] = configuration.getString(entry.name() + ".text", messageData.text);
            messages[entry.ordinal()] = messages[entry.ordinal()].replace('$', (char) 0x00A7);

            if (messageData.getNotes() != null) {
                messageData.setNotes(configuration.getString(entry.name() + ".notes", messageData.getNotes()));
                configuration.set(entry.name() + ".notes", messageData.getNotes());
            }
            configuration.set(entry.name() + ".text", messages[entry.ordinal()]);
        }

        try {
            configuration.save(Constants.MESSAGES_FILE_PATH);
        } catch (IOException exception) {
            // todo
        }
    }

    @Override
    public void executeCritical() {

    }

}
